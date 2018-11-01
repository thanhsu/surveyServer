package com.survey.dbservices.action;

import com.survey.dbservice.dao.UserDao;
import com.survey.utils.FieldName;

import io.vertx.core.json.JsonObject;

public class ChangePassword extends BaseDbServiceAction {

	@Override
	public void doProcess(JsonObject body) {
		String lvUserName = body.getString(FieldName.USERNAME);
		String lvOldPassword = body.getString(FieldName.OLDPASSWORD);
		String lvNewPassword = body.getString(FieldName.PASSWORD);
		UserDao lvUserDao = new UserDao();
		mvResponse = lvUserDao.changePassword(lvUserName, lvOldPassword, lvNewPassword);
	}

}
