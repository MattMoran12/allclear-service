package app.allclear.common.redis;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Value object that represents the configuration properties for the Redis cache layer.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/22/2020
 *
 */

public class RedisConfig implements Serializable
{
	private static final long serialVersionUID = 1L;

	public static final int PORT_DEFAULT = 6379;

	public final String host;
	public final int port;
	public final Long timeout;	// Redis's operational timeout.
	public final Integer poolSize;
	public final boolean ssl;
	public final boolean testWhileIdle;
	public final boolean test;

	/** Populator.
	 * 
	 * @param host
	 * @param port
	 * @param timeout
	 * @param poolSize
	 * @param ssl
	 * @param testWhileIdle
	 * @param test
	 */
	public RedisConfig(@JsonProperty("host") final String host,
		@JsonProperty("port") final Integer port,
		@JsonProperty("timeout") final Long timeout,
		@JsonProperty("poolSize") final Integer poolSize,
		@JsonProperty("ssl") final Boolean ssl,
		@JsonProperty("testWhileIdle") final Boolean testWhileIdle,
		@JsonProperty("test") final Boolean test)
	{
		this.host = host;
		this.port = (null != port) ? port : PORT_DEFAULT;
		this.timeout = timeout;
		this.poolSize = poolSize;
		this.ssl = (null != ssl) ? ssl : false;
		this.testWhileIdle = (null != testWhileIdle) ? testWhileIdle :  true;
		this.test = (null != test) ? test : false;
	}

	public RedisConfig(final String host, final int port, final Long timeout, final Integer poolSize)
	{
		this(host, port, timeout, poolSize, null, null, null);
	}
}