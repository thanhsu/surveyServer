package com.survey.utils;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;

public class SurveyGoogleApiUtils {
	private static SurveyGoogleApiUtils instance = null;
	private String applicationGoogleToken = null;
	GoogleIdTokenVerifier googleTokenVerifier = null;

	public static SurveyGoogleApiUtils getInstance() {
		if (instance == null) {
			synchronized (SurveyGoogleApiUtils.class) {
				instance = new SurveyGoogleApiUtils();
			}
		}
		return instance;
	}

	public void init(String token, String clientID) throws GeneralSecurityException, IOException {
		instance.applicationGoogleToken = token;
		final NetHttpTransport transport = GoogleNetHttpTransport.newTrustedTransport();
		final JacksonFactory jsonFactory = JacksonFactory.getDefaultInstance();

		googleTokenVerifier = new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
				.setAudience(Collections.singletonList(clientID)).build();
	}

	public GoogleUserBean retrieveUserInfo(String userToken) throws GeneralSecurityException, IOException {
		final GoogleIdToken googleIdToken = googleTokenVerifier.verify(userToken);

		if (googleIdToken == null) {
			return null;
		}
		final Payload payload = googleIdToken.getPayload();
		final Boolean emailVerified = payload.getEmailVerified();

		if (emailVerified) {
			GoogleUserBean lvBean = new GoogleUserBean();
			lvBean.setUserToken(userToken);
			lvBean.setEmail(payload.getEmail());
			lvBean.setFullname((String) payload.get("name"));
			lvBean.setDateOfBirth("");
			lvBean.setAvatar((String) payload.get("picture"));

			return lvBean;
		}
		return null;
	}

}
