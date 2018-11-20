package com.survey.dbservices.action;

import com.survey.dbservice.dao.UserDao;
import com.survey.utils.FieldName;

import io.vertx.core.json.JsonObject;

public class UserInfoAction extends BaseAdminServiceAction {

	@Override
	public void doProcess(JsonObject body) {
		UserDao lvDao = new UserDao();
		if (body.getString(FieldName.ID) != null) {
			lvDao.doGetUserInfo(body.getString(FieldName.ID));
		} else {
			lvDao.doGetUserInfobyUserName(body.getString(FieldName.USERNAME));
		}
		mvResponse = lvDao.getMvFutureResponse();
	}

}
