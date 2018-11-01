package com.survey.otp;

import java.io.Serializable;
import java.util.Date;

public class OTPUser implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5316923622635778952L;
	private String loginID;
	private String sessionID;
	private Date createDate;
	private String otp;
	private String secret;
	private int failedCount = 0;

	public OTPUser(String pLoginID, String pSessionID, Date pCreateDate, String pOtp, String pSecret, int pFail) {
		this.setLoginID(pLoginID);
		this.setSessionID(pSessionID);
		this.setCreateDate(pCreateDate);
		this.setOtp(pOtp);
		this.setSecret(pSecret);
		this.setFailedCount(pFail);
	}

	public OTPUser() {
	}

	public String getLoginID() {
		return loginID;
	}

	public void setLoginID(String clientID) {
		this.loginID = clientID;
	}

	public String getSessionID() {
		return sessionID;
	}

	public void setSessionID(String sessionID) {
		this.sessionID = sessionID;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public String getOtp() {
		return otp;
	}

	public void setOtp(String otp) {
		this.otp = otp;
	}

	public String getSecret() {
		return secret;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public int getFailedCount() {
		return failedCount;
	}

	public void setFailedCount(int failedCount) {
		this.failedCount = failedCount;
	}

}
