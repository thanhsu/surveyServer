package com.survey.dbservices.action;

import com.survey.dbservice.dao.UserDao;
import com.survey.utils.FieldName;

import io.vertx.core.json.JsonObject;

public class ResetPasswordAction extends BaseAdminServiceAction {

	@Override
	public void doProcess(JsonObject body) {
		String username = body.getString(FieldName.USERNAME);
		String newPassword = body.getString(FieldName.PASSWORD);
		UserDao lvDao = new UserDao();
		lvDao.ResetPassword(username, newPassword);
		mvResponse = lvDao.getMvFutureResponse();
	}

}
