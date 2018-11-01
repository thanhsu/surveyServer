package com.survey.otp;

public class OTPVerifyResp {
	private Boolean success;
	private int failRemain;
	private String message;

	public Boolean getSuccess() {
		return success;
	}

	public void setSuccess(Boolean success) {
		this.success = success;
	}

	public int getFailRemain() {
		return failRemain;
	}

	public void setFailRemain(int failRemain) {
		this.failRemain = failRemain;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
