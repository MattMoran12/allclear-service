package app.allclear.common.redis;

import org.junit.Assert;
import org.junit.jupiter.api.*;

import app.allclear.redis.JedisConfig;

/** Unit test that verifies the RedisConfig POJO.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/22/2020
 *
 */

public class RedisConfigTest
{
	@Test
	public void create()
	{
		var value = new JedisConfig("host1", 123, 105L, 15, null, true, false, true);

		Assert.assertNotNull("Exists", value);
		Assert.assertEquals("Check host", "host1", value.host);
		Assert.assertEquals("Check port", 123, value.port);
		Assert.assertEquals("Check timeout", Long.valueOf(105L), value.timeout);
		Assert.assertEquals("Check poolSize", Integer.valueOf(15), value.poolSize);
		Assertions.assertNull(value.password, "Check password");
		Assert.assertTrue("Check ssl", value.ssl);
		Assert.assertFalse("Check testWhileIdle", value.testWhileIdle);
		Assert.assertTrue("Check test", value.test);
	}

	@Test
	public void createWithDefaults()
	{
		var value = new JedisConfig("host2", null, null, null, "1a2b3c", null, null, null);

		Assert.assertNotNull("Exists", value);
		Assert.assertEquals("Check host", "host2", value.host);
		Assert.assertEquals("Check port", 6379, value.port);
		Assert.assertNull("Check timeout", value.timeout);
		Assert.assertNull("Check poolSize", value.poolSize);
		Assertions.assertEquals("1a2b3c", value.password, "Check password");
		Assert.assertFalse("Check ssl", value.ssl);
		Assert.assertTrue("Check testWhileIdle", value.testWhileIdle);
		Assert.assertFalse("Check test", value.test);
	}
}
