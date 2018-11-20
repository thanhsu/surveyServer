package com.survey.dbservices.action;

import com.survey.dbservice.dao.UserDao;
import com.survey.utils.FieldName;

import io.vertx.core.json.JsonObject;

public class RegisterAction extends BaseAdminServiceAction {

	@Override
	public void doProcess(JsonObject body) {
		UserDao mvUserDao;
		mvUserDao = new UserDao();
		String lvUsername = body.getString(FieldName.USERNAME);
		String lvPass = body.getString(FieldName.PASSWORD);
		String lvEmail = body.getString(FieldName.EMAIL);
		String lvFullname = body.getString(FieldName.FULLNAME);
		mvUserDao.doRegister(lvUsername, lvPass, lvEmail, lvFullname);
		mvResponse = mvUserDao.getMvFutureResponse();
	}

}
