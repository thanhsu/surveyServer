package com.survey.utils;

public enum CodeMapping {
	C0000("Request Success"),
	C1111("Request Error"),
	C2222("Action not found"),
	C3333("Param Error"),
	C4444("Exception"),
	C5555("Method Not Support"),
	C6666("Permission Denied"),
	C7777("Session Timeout"),
	
	U0000("Everythings ok"),
	U1111("Username not found"),
	U2222("Password wrong"),
	U3333("User Locked"),
	U4444("Email not found"),
	U5555("Change password fail"),
	U6666("Reset password fail"),
	U7777("Reset password Error, Email Not Match"),
	U8888("Reset password Error, Email Not Access"),
	U9999("Reset password Error, Invalid Token Or Token Expired, try Again"),
	U000A("Update user status error, Invalid Token Or Token Expired, try Again"),
	U000B("Active user error, Invalid Token"),
	
	R1111("Username is exists"),
	R2222("Register Fail"),
	R3333("Email is exists"),
	R0000("Register Success"),
	
	S0000("Everything oke"),
	S1111("Survey not found"),
	S2222("Permission Denied"),
	S3333("Survey Expired"),
	S3334("Survey have not start"),
	S3335("Survey is out of money"),
	S4444("Survey Deny Multi Response"),
	S5555("Check point remain for continute"),
	S6666("Survey not allow for submit now"),
	S7777("Initial Fun greater than account balance"),
	S8888("Survey Closed or Deleted"),
	S9999("Pushlish reject. Because Survey was pushlished"),
	S0001("Survey can not restore"),
	S0002("Survey status not allow Deposit now"),
	
	D1111("Deposit Not Found"),
	
	W1111("Withdraw amount failed"),
	W2222("Withdraw transaction can not cancel"),
	
	P0000("Proxy ok"),
	P1111("Proxy data to Ethe server error"),
	P2222("Request Error")
	;
	
	
	private final String value;

	private CodeMapping(String v) {
		this.value = v;
	}

	public String value() {
		return this.value;
	}

	public static CodeMapping fromValue(String v) {
		try {
			return valueOf(v);
		} catch (Exception e) {
			return null;
		}
	}

}
