package app.allclear.platform.value;

import static org.fest.assertions.api.Assertions.assertThat;

import java.util.Date;

import org.junit.jupiter.api.*;

import app.allclear.common.ThreadUtils;
import app.allclear.platform.model.StartRequest;

/** Unit test class that verifies the SessionValue POJO.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/25/2020
 *
 */

public class SessionValueTest
{
	@Test
	public void create_people()
	{
		var o = new SessionValue(false, new PeopleValue("stevie", "888-555-0001", true));
		assertThat(o.id).as("Check ID").hasSize(36);
		Assertions.assertFalse(o.rememberMe, "Check rememberMe");
		Assertions.assertEquals(SessionValue.DURATION_SHORT, o.duration, "Check duration");
		Assertions.assertEquals(30 * 60, o.seconds(), "Check seconds");
		Assertions.assertNotNull(o.person, "Check person");
		Assertions.assertEquals("stevie", o.person.name, "Check person.name");
		Assertions.assertEquals("888-555-0001", o.person.phone, "Check person.phone");
		Assertions.assertTrue(o.person.active, "Check person.active");
		Assertions.assertNull(o.registration, "Check registration");
		assertThat(o.expiresAt).as("Check expiresAt").isCloseTo(new Date(System.currentTimeMillis() + SessionValue.DURATION_SHORT), 100L);
		assertThat(o.lastAccessedAt).as("Check lastAccessedAt").isCloseTo(new Date(), 100L).isEqualTo(o.createdAt);

		ThreadUtils.sleep(2000L);

		var expiresAt = new Date(o.expiresAt.getTime());
		var lastAccessedAt = new Date(o.lastAccessedAt.getTime());
		assertThat(o.accessed().expiresAt).as("Check accessed: expiresAt").isAfter(expiresAt);
		assertThat(o.accessed().lastAccessedAt).as("Check accessed: lastAccessedAt").isAfter(lastAccessedAt);
	}

	@Test
	public void create_people_rememberMe()
	{
		var o = new SessionValue(true, new PeopleValue("moira", "888-555-0002", false));
		assertThat(o.id).as("Check ID").hasSize(36);
		Assertions.assertTrue(o.rememberMe, "Check rememberMe");
		Assertions.assertEquals(SessionValue.DURATION_LONG, o.duration, "Check duration");
		Assertions.assertEquals(30 * 24 * 60 * 60, o.seconds(), "Check seconds");
		Assertions.assertNotNull(o.person, "Check person");
		Assertions.assertEquals("moira", o.person.name, "Check person.name");
		Assertions.assertEquals("888-555-0002", o.person.phone, "Check person.phone");
		Assertions.assertFalse(o.person.active, "Check person.active");
		Assertions.assertNull(o.registration, "Check registration");
		assertThat(o.expiresAt).as("Check expiresAt").isCloseTo(new Date(System.currentTimeMillis() + SessionValue.DURATION_LONG), 100L);
		assertThat(o.lastAccessedAt).as("Check lastAccessedAt").isCloseTo(new Date(), 100L).isEqualTo(o.createdAt);

		ThreadUtils.sleep(2000L);

		var expiresAt = new Date(o.expiresAt.getTime());
		var lastAccessedAt = new Date(o.lastAccessedAt.getTime());
		assertThat(o.accessed().expiresAt).as("Check accessed: expiresAt").isAfter(expiresAt);
		assertThat(o.accessed().lastAccessedAt).as("Check accessed: lastAccessedAt").isAfter(lastAccessedAt);
	}

	@Test
	public void create_registration()
	{
		var o = new SessionValue(new StartRequest("888-555-0003", true, true));
		assertThat(o.id).as("Check ID").hasSize(36);
		Assertions.assertFalse(o.rememberMe, "Check rememberMe");
		Assertions.assertEquals(SessionValue.DURATION_SHORT, o.duration, "Check duration");
		Assertions.assertEquals(30 * 60, o.seconds(), "Check seconds");
		Assertions.assertNull(o.person, "Check person");
		Assertions.assertNotNull(o.registration, "Check registration");
		Assertions.assertEquals("888-555-0003", o.registration.phone, "Check registration.phone");
		Assertions.assertTrue(o.registration.beenTested, "Check registration.beenTested");
		Assertions.assertTrue(o.registration.haveSymptoms, "Check registration.haveSymptoms");
		assertThat(o.expiresAt).as("Check expiresAt").isCloseTo(new Date(System.currentTimeMillis() + SessionValue.DURATION_SHORT), 100L);
		assertThat(o.lastAccessedAt).as("Check lastAccessedAt").isCloseTo(new Date(), 100L).isEqualTo(o.createdAt);

		ThreadUtils.sleep(2000L);

		var expiresAt = new Date(o.expiresAt.getTime());
		var lastAccessedAt = new Date(o.lastAccessedAt.getTime());
		assertThat(o.accessed().expiresAt).as("Check accessed: expiresAt").isAfter(expiresAt);
		assertThat(o.accessed().lastAccessedAt).as("Check accessed: lastAccessedAt").isAfter(lastAccessedAt);

		ThreadUtils.sleep(2000L);

		var s = o.promote(true, new PeopleValue("johnny", "888-555-0004", true));
		assertThat(s.id).as("Check ID").hasSize(36);
		Assertions.assertTrue(s.rememberMe, "Check rememberMe");
		Assertions.assertEquals(SessionValue.DURATION_LONG, s.duration, "Check duration");
		Assertions.assertEquals(30 * 24 * 60 * 60, s.seconds(), "Check seconds");
		Assertions.assertNotNull(s.person, "Check person");
		Assertions.assertEquals("johnny", s.person.name, "Check person.name");
		Assertions.assertEquals("888-555-0004", s.person.phone, "Check person.phone");
		Assertions.assertTrue(s.person.active, "Check person.active");
		Assertions.assertNull(s.registration, "Check registration");
		assertThat(s.expiresAt).as("Check expiresAt").isCloseTo(new Date(System.currentTimeMillis() + SessionValue.DURATION_LONG), 100L).isAfter(o.expiresAt);
		assertThat(s.lastAccessedAt).as("Check lastAccessedAt").isCloseTo(new Date(), 100L).isAfter(o.createdAt).isAfter(o.lastAccessedAt);

		ThreadUtils.sleep(2000L);

		var ss = o.promote(false, new PeopleValue("barbara", "888-555-0005", true));
		assertThat(ss.id).as("Check ID").hasSize(36);
		Assertions.assertFalse(ss.rememberMe, "Check rememberMe");
		Assertions.assertEquals(SessionValue.DURATION_SHORT, ss.duration, "Check duration");
		Assertions.assertEquals(30 * 60, ss.seconds(), "Check seconds");
		Assertions.assertNotNull(ss.person, "Check person");
		Assertions.assertEquals("barbara", ss.person.name, "Check person.name");
		Assertions.assertEquals("888-555-0005", ss.person.phone, "Check person.phone");
		Assertions.assertTrue(ss.person.active, "Check person.active");
		Assertions.assertNull(ss.registration, "Check registration");
		assertThat(ss.expiresAt).as("Check expiresAt").isCloseTo(new Date(System.currentTimeMillis() + SessionValue.DURATION_SHORT), 100L).isAfter(o.expiresAt);
		assertThat(ss.lastAccessedAt).as("Check lastAccessedAt").isCloseTo(new Date(), 100L).isAfter(o.createdAt).isAfter(s.lastAccessedAt);
	}
}
