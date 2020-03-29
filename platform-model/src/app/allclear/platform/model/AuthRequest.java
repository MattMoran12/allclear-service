package app.allclear.platform.model;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Value object that represents the request to start the authentication process via magic link.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/29/2020
 *
 */

public class AuthRequest implements Serializable
{
	private static final long serialVersionUID = 1L;

	public final String phone;	// If the phone number is provided, the magic link is sent via SMS.
	public final String email;	// If the email address is provided, the magic link is sent via Email. NOT IMPLEMENTED yet.

	public AuthRequest(@JsonProperty("phone") final String phone,
		@JsonProperty("email") final String email)
	{
		this.phone = StringUtils.trimToNull(phone);
		this.email = StringUtils.trimToNull(email);
	}

	public AuthRequest(final String phone) { this(phone, null); }
}