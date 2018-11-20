package com.survey.dbservices.action;

import java.sql.Date;

import com.survey.dbservice.dao.UserDao;
import com.survey.utils.FieldName;

import io.vertx.core.json.JsonObject;

public class UpdateUserInfo extends BaseAdminServiceAction {

	@Override
	public void doProcess(JsonObject body) {
		UserDao lvDao = new UserDao();
		lvDao.updateUserInfo(body.getString(FieldName.ID), body.getString(FieldName.FULLNAME),
				Date.valueOf(body.getString(FieldName.BIRTHDATE)), body.getString(FieldName.NATIONAL),
				body.getString(FieldName.WARD_CITY));
		mvResponse = lvDao.getMvFutureResponse();
	}

}
