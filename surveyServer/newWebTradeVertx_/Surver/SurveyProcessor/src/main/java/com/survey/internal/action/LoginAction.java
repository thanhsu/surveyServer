package com.survey.internal.action;

import com.survey.constant.HttpParameter;
import com.survey.dbservice.dao.UserDao;
import com.survey.dbservices.action.UserLogin;
import com.survey.utils.CodeMapping;
import com.survey.utils.FieldName;

import io.vertx.core.json.JsonObject;

public class LoginAction extends InternalSurveyBaseAction {

	public LoginAction() {
	}

	@Override
	public void doProccess() {
		// response = Future.future();
		String methodLogin = getMessageBody().getString(FieldName.METHOD);
		if (methodLogin == null) {
			defaultLogin();
		} else if (methodLogin.equals("google")) {
			loginByGoogle();
		} else if (methodLogin.equals("facebook")) {
			loginByFacebook();
		} else {
			defaultLogin();
		}
	}

	private void defaultLogin() {
		loginID = getMessageBody().getString(HttpParameter.LOGINID);
		// String lvPass = getMessageBody().getString(HttpParameter.PASSWORD);
		// Check Message Login
		UserLogin lvLogin = new UserLogin();
		lvLogin.doProcess(getMessageBody());

		lvLogin.getMvResponse().setHandler(handler -> {
			if (handler.succeeded()) {
				JsonObject msg = handler.result();
				if (msg.getString(FieldName.CODE).equals(CodeMapping.C0000)) {
					response.complete(msg);
				} else {
					response.complete(msg);
				}
			} else {
				response.fail(handler.cause());
			}
		});
	}

	private void loginByGoogle() {
		// set username = google email and email = google email
		// get user token and check from user table
		String userToken = getMessageBody().getString(FieldName.CLIENT_TOKEN);

		UserDao lvUserDao = new UserDao();
		lvUserDao.loginByGoogle(userToken);
		lvUserDao.getMvFutureResponse().setHandler(handler -> {
			if (handler.succeeded()) {
				JsonObject msg = handler.result();
				if (msg.getString(FieldName.CODE).equals(CodeMapping.C0000)) {
					response.complete(msg);
				} else {
					response.complete(msg);
				}
			} else {
				response.fail(handler.cause());
			}
		});
	}

	private void loginByFacebook() {

	}

}
