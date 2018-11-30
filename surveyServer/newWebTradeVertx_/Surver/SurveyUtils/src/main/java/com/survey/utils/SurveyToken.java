package com.survey.utils;

import java.security.NoSuchAlgorithmException;

import javax.crypto.KeyGenerator;

import com.survey.otp.OTPManagerException;
import com.survey.otp.Utils;

import io.vertx.core.json.JsonObject;

public class SurveyToken {
	private static SurveyToken intance = null;
	private JsonObject config = null;

	public static SurveyToken getInstance() {
		synchronized (SurveyToken.class) {
			if (intance == null) {
				intance = new SurveyToken();
			}
		}
		return intance;
	}

	public void init() {

	}

	public String registerToken(String username) throws OTPManagerException {
		String token = Utils.generateSharedSecret();
		return token;
	}
	
	public String cashTokenGenerate(String username)  {
		String token = "";
		
		return token;
		
	}
	
	public String generateNewPassword() {
		String newPassword =Utils.generateOTPSMSMessage(8)[1];
		return newPassword;
	}
}
