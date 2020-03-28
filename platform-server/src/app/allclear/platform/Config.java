package app.allclear.platform;

import java.io.Serializable;

import io.dropwizard.Configuration;
import io.dropwizard.db.DataSourceFactory;

import com.fasterxml.jackson.annotation.JsonProperty;

import app.allclear.common.DWUtil;
import app.allclear.common.ManifestUtils;
import app.allclear.common.value.ManifestValue;

/** Value object that represents the application configuration properties.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/22/2020
 *
 */

public class Config extends Configuration implements Serializable
{
	private static final long serialVersionUID = 1L;

	public final String env;
	public final boolean disableSwagger;
	public final DataSourceFactory trans;
	public final ManifestValue manifest;

	public final String baseUrl;
	public final String registrationPhone;
	public final String authenticationPhone;
	public final String registrationSMSMessage;
	public final String authenticationSMSMessage;

	public String getVersion() { return manifest.version; }

	public Config(@JsonProperty("env") final String env,
		@JsonProperty("disableSwagger") final Boolean disableSwagger,
		@JsonProperty("trans") final DataSourceFactory trans,
		@JsonProperty("baseUrl") final String baseUrl,
		@JsonProperty("registrationPhone") final String registrationPhone,
		@JsonProperty("authenticationPhone") final String authenticationPhone)
	{
		this.env = env;
		this.trans = trans;
		this.manifest = ManifestUtils.getInfo(getClass());
		this.disableSwagger = Boolean.TRUE.equals(disableSwagger);

		this.baseUrl = baseUrl;
		this.registrationPhone = registrationPhone;
		this.authenticationPhone = authenticationPhone;
		this.registrationSMSMessage = DWUtil.load(Config.class.getResource("/messages/sms/registration.txt")).replace("${baseUrl}", baseUrl);
		this.authenticationSMSMessage = DWUtil.load(Config.class.getResource("/messages/sms/authentication.txt")).replace("${baseUrl}", baseUrl);
	}
}
