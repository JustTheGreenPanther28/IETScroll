package com.ietscroll.security;

public class SecurityConstaints {
	public static final long EXPIRATION_TIME = 9123372036854771807L;
	public static final long PASSWORD_EXPIRATION_TIME = 360000;
	public static final String USER_ID = "USER ID";
	public static final String TOKEN_PREFIX = "Bearer ";
	public static final String HEADER_STRING = "Authorization";
	public static final String SIGN_UP_URL = "/api/v1/user/register";
	public static final String EMAIL_VERIFICATION = "/api/v1/otp/verify";
	public static final String LOGIN = "/login";
	public static final String RESEND_OTP = "/api/v1/otp/resend";
	public static final String ADMIN_APIs = "/api/v1/admin/**";
}
