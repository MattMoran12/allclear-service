package app.allclear.platform.dao;

import static java.net.URLEncoder.encode;
import static java.nio.charset.StandardCharsets.UTF_8;
import static app.allclear.common.jackson.JacksonUtils.timestamp;

import java.io.IOException;
import java.util.Date;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.RandomStringUtils;

import redis.clients.jedis.Jedis;

import com.fasterxml.jackson.databind.ObjectMapper;

import app.allclear.common.dao.QueryResults;
import app.allclear.common.errors.*;
import app.allclear.common.jackson.JacksonUtils;
import app.allclear.common.redis.RedisClient;
import app.allclear.platform.Config;
import app.allclear.platform.filter.SessionFilter;
import app.allclear.platform.model.StartRequest;
import app.allclear.platform.value.*;
import app.allclear.twilio.client.TwilioClient;
import app.allclear.twilio.model.SMSRequest;
import redis.clients.jedis.ScanParams;

/** Data access object that manages user sessions.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/25/2020
 *
 */

public class SessionDAO
{
	private static final String ID = "session:%s";
	private static final String MATCH = "session:*";
	public static final int MAX_TRIES = 3;
	private static final int AUTH_DURATION = 5 * 60;	// Five minutes
	private static final int ALERT_DURATION = 24 * 60 * 60;	// 24 hours
	private static final String AUTH_KEY = "authentication:%s:%s";
	private final ObjectMapper mapper = JacksonUtils.createMapper();
	public static final int TOKEN_LENGTH = RegistrationDAO.CODE_LENGTH;
	public static final SessionValue ANONYMOUS = SessionValue.anonymous();

	public static String key(final String id) { return String.format(ID, id); }
	public static String authKey(final String phone, final String token) { return String.format(AUTH_KEY, phone, token); }

	private final Config conf;
	private final RedisClient redis;
	private final TwilioClient twilio;
	private final ThreadLocal<SessionValue> current = new ThreadLocal<>();

	public SessionDAO(final RedisClient redis, final Config conf) { this(redis, null, conf); }
	public SessionDAO(final RedisClient redis, final TwilioClient twilio, final Config conf)
	{
		this.conf = conf;
		this.redis = redis;
		this.twilio = twilio;
	}

	/** Adds a short-term registration session.
	 * 
	 * @param request
	 * @return never NULL.
	 */
	public SessionValue add(final StartRequest request)
	{
		return add(new SessionValue(request));
	}

	/** Adds a regular user session.
	 * 
	 * @param person
	 * @param rememberMe
	 * @return never NULL.
	 */
	public SessionValue add(final PeopleValue person, final boolean rememberMe)
	{
		return add(new SessionValue(rememberMe, person));
	}

	/** Adds an administrative user session.
	 * 
	 * @param admin
	 * @param rememberMe
	 * @return never NULL.
	 */
	public SessionValue add(final AdminValue admin, final boolean rememberMe)
	{
		return add(new SessionValue(rememberMe, admin));
	}

	SessionValue add(final SessionValue value)
	{
		try
		{
			redis.put(key(value.id), mapper.writeValueAsString(value), value.seconds());
		}
		catch (final IOException ex) { throw new RuntimeException(ex); }

		return value;
	}

	/** Sends an authentication token to the specified phone number/user.
	 * 
	 * @param phone
	 * @return the token to be used in tests.
	 */
	public String auth(final String phone)
	{
		return redis.operation(j -> {
			limit(j, phone);

			var token = RandomStringUtils.randomNumeric(TOKEN_LENGTH).toUpperCase();

			twilio.send(new SMSRequest(conf.authSid, conf.authPhone, String.format(conf.authSMSMessage, token, encode(phone, UTF_8), encode(token, UTF_8)), phone));
			j.setex(authKey(phone, token), AUTH_DURATION, phone);

			return token;
		});
	}

	/** Sends an alert message with an authentication token to the specified phone number/user.
	 * 
	 * @param phone
	 * @param lastAlertedAt
	 * @return the token to be used in tests.
	 */
	public String alert(final String phone, final Date lastAlertedAt)
	{
		var token = RandomStringUtils.randomNumeric(TOKEN_LENGTH).toUpperCase();

		twilio.send(new SMSRequest(conf.alertSid, conf.alertPhone,
			String.format(conf.alertSMSMessage, encode(timestamp(lastAlertedAt), UTF_8), encode(phone, UTF_8), encode(token, UTF_8)), phone));
		redis.put(authKey(phone, token), phone, ALERT_DURATION);

		return token;
	}

	/** Confirms the authentication token.
	 * 
	 * @param phone
	 * @param token
	 * @throws ValidationException if the phone/token combination cannot be found
	 */
	public void auth(final String phone, final String token) throws ValidationException
	{
		var key = authKey(phone, token);
		redis.operation(j -> {
			if (!j.exists(key)) throw new ValidationException("Confirmation failed.");	// Do NOT give too much information on bad authentication requests.
			return j.del(key);
		});
	}

	/** Promotes a registration session to a person session maintaining the same ID.
	 * 
	 * @param person
	 * @param rememberMe
	 * @return never NULL
	 */
	public SessionValue promote(final PeopleValue person, final boolean rememberMe)
	{
		try
		{
			var o = get().promote(rememberMe, person);
			redis.put(key(o.id), mapper.writeValueAsString(o), o.seconds());

			return o;
		}
		catch (final IOException ex) { throw new RuntimeException(ex); }
	}

	/** Gets the session value associated with the current thread.
	 * 
	 * @return NULL if there is no current session
	 */
	public SessionValue current() { return current.get(); }

	/** Gets the current session if available otherwise retrieves the Anonymous session.
	 * 
	 * @return never NULL.
	 */
	public SessionValue currentOrAnon()
	{
		var o = current();
		return (null != o) ? o : ANONYMOUS;
	}

	/** Internal/test usage - sets the current threads associated session.
	 * 
	 * @param value
	 * @return the supplied session value
	 */
	public SessionValue current(final SessionValue value)
	{
		current.set(value);

		return value;
	}

	public SessionValue current(final CustomerValue value) { return current(new SessionValue(value)); }
	SessionValue current(final PeopleValue value) { return current(new SessionValue(false, value)); }	// For tests

	/** Set the current threads associated session based on the supplied session ID.
	 * 
	 * @param id
	 * @return never NULL
	 * @throws NotAuthenticatedException
	 */
	public SessionValue current(final String id) throws NotAuthenticatedException
	{
		return current(get(id));
	}

	/** Checks that the current session is associated with an Admin.
	 * 
	 * @return never NULL
	 * @throws NotAuthorizedException
	 */
	public AdminValue checkAdmin() throws NotAuthorizedException
	{
		var o = current();
		if ((null == o) || !o.canAdmin()) throw new NotAuthorizedException("Must be an Admin session.");

		return o.admin;
	}

	/** Checks that the current session is associated with an Admin or Person - NOT an Editor.
	 * 
	 * @return never NULL
	 * @throws NotAuthorizedException
	 */
	public SessionValue checkAdminOrPerson() throws NotAuthorizedException
	{
		var o = current();
		if ((null != o) && (o.person() || o.canAdmin())) return o;

		throw new NotAuthorizedException("Must be an Admin or Person session.");
	}

	/** Checks that the current session is associated with an Editor.
	 * 
	 * @return never NULL
	 * @throws NotAuthorizedException
	 */
	public AdminValue checkEditor() throws NotAuthorizedException
	{
		var o = current();
		if ((null == o) || !o.admin()) throw new NotAuthorizedException("Must be an Editor session.");	// All Admins account can act as Editor.

		return o.admin;
	}

	/** Checks that the current session is associated with an Admin, Editor, or Person - NOT an Editor.
	 * 
	 * @return never NULL
	 * @throws NotAuthorizedException
	 */
	public SessionValue checkEditorOrPerson() throws NotAuthorizedException
	{
		var o = current();
		if ((null != o) && (o.person() || o.admin())) return o;

		throw new NotAuthorizedException("Must be an Admin, Editor, or Person session.");
	}

	/** Checks that the current session is associated with a Person.
	 * 
	 * @return never NULL
	 * @throws NotAuthorizedException
	 */
	public PeopleValue checkPerson() throws NotAuthorizedException
	{
		var o = current();
		if ((null == o) || !o.person()) throw new NotAuthorizedException("Must be a Person session.");

		return o.person;
	}

	/** Checks that the current session is associated with a Super-Admin.
	 * 
	 * @return never NULL
	 * @throws NotAuthorizedException
	 */
	public AdminValue checkSuper() throws NotAuthorizedException
	{
		var o = checkAdmin();
		if ((null == o) || !o.supers) throw new NotAuthorizedException("Must be a Super-Admin session.");

		return o;
	}

	/** Clears the current session. */
	public void clear()
	{
		current.remove();
	}

	/** Confirms existence of the specified session ID.
	 * 
	 * @param id
	 * @return never NULL
	 */
	public boolean exists(final String id) { return redis.containsKey(key(id)); }

	/** Retrieves a session without extending its expiration.
	 * 
	 * @param id
	 * @return NULL if not found.
	 */
	public SessionValue find(final String id) { return redis.operation(j -> findByKey(j, key(id))); }
	private SessionValue findByKey(final Jedis jedis, final String key)
	{
		var v = jedis.get(key);

		try { return (null != v) ? mapper.readValue(v, SessionValue.class) : null; }
		catch (final IOException ex) { throw new RuntimeException(ex); }
	}

	/** Gets the current session.
	 * 
	 * @return never NULL
	 * @throws NotAuthenticatedException
	 */
	public SessionValue get() throws NotAuthenticatedException
	{
		var v = current.get();
		if (null == v) throw new NotAuthenticatedException("No current session is available.");

		return v;
	}

	/** Gets the session value by the specified ID and extends the expiration.
	 * 
	 * @param id
	 * @return never NULL
	 * @throws NotAuthenticatedException if not found
	 */
	public SessionValue get(final String id) throws NotAuthenticatedException
	{
		return redis.operation(j -> {
			var key = key(id);
			var v = j.get(key);
			if (null == v) throw new NotAuthenticatedException("The ID '" + id + "' is invalid.");

			try
			{
				var o = mapper.readValue(v, SessionValue.class);

				o.accessed();
				j.setex(key, o.seconds(), mapper.writeValueAsString(o));

				return o;
			}
			catch (final IOException ex) { throw new RuntimeException(ex); }
		});
	}

	/** Removes the current session. */
	public void remove()
	{
		var v = current();
		if (null != v) remove(v.id);
	}

	/** Removes a single session.
	 * 
	 * @param id
	 */
	public void remove(final String id)
	{
		redis.remove(key(id));
	}

	/** Updates a session - called when an application user updates their profile.
	 * 
	 * @param value
	 * @param person
	 * @return the new session object.
	 */
	public SessionValue update(final SessionValue value, final PeopleValue person)
	{
		var o = new SessionValue(value.id,
			value.rememberMe,
			value.duration,
			null,
			person,
			null,
			null,
			value.expiresAt,
			value.lastAccessedAt,
			value.createdAt);

		try { redis.put(key(o.id), mapper.writeValueAsString(o), o.seconds()); }
		catch (final IOException ex) { throw new RuntimeException(ex); }

		return o;
	}

	/** Searches the user sessions.
	 * 
	 * @param filter
	 * @return never NULL.
	 */
	public QueryResults<SessionValue, SessionFilter> search(final SessionFilter filter)
	{
		filter.clean();
		return redis.operation(j -> {
			var cursor = (filter.page() - 1) + "";
			var match = (null != filter.id) ? key(filter.id + "*") : MATCH;
			var o = j.scan(cursor, new ScanParams().count(filter.pageSize(100)).match(match));

			if (CollectionUtils.isEmpty(o.getResult())) return new QueryResults<SessionValue, SessionFilter>(0L, filter);

			var v = o.getResult().stream().map(i -> findByKey(j, i)).collect(Collectors.toList());
			var r = new QueryResults<SessionValue, SessionFilter>(v, filter);
			r.page = Integer.parseInt(o.getCursor());
			r.pages++;	// Always add one.

			return r;
		});
	}

	/** Counts the number of authentication requests currently available for the specified phone number.
	 * 
	 * @param phone
	 * @return 0 if none found.
	 */
	public long count(final String phone)
	{
		return (long) redis.operation(j -> count(j, phone));
	}

	int count(final Jedis j, final String phone)
	{
		return CollectionUtils.size(j.scan("", new ScanParams().count(100).match(authKey(phone, "*"))).getResult());
	}

	void limit(final Jedis j, final String phone) throws ValidationException
	{
		if (MAX_TRIES <= count(j, phone)) throw new ValidationException("phone", "Too many authentication requests for phone number '" + phone + "'");
	}
}
