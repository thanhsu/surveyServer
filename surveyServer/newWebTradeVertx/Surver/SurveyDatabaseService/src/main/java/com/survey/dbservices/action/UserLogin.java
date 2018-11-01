package com.survey.dbservices.action;

import com.survey.dbservice.dao.UserDao;
import com.survey.utils.FieldName;

import io.vertx.core.json.JsonObject;

public class UserLogin extends BaseDbServiceAction {
	

	@Override
	public void doProcess(JsonObject body) {
		UserDao mvUserDao;
		mvUserDao = new UserDao();
		String lvUsername = body.getString(FieldName.USERNAME);
		String lvPass = body.getString(FieldName.PASSWORD);
		mvResponse = mvUserDao.doLogin(lvUsername, lvPass);
	}

}
