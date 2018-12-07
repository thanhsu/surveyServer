package com.survey.internal.action;

import com.survey.dbservice.dao.UserDao;
import com.survey.utils.FieldName;

public class RetrieveAccountBaseInfo extends InternalSurveyBaseAction {

	@Override
	public void doProccess() {
		String target = getMessageBody().getString(FieldName.TOUSER);
		UserDao lvUserDao = new UserDao();
		lvUserDao.doGetUserBaseInfo(target);
		lvUserDao.getMvFutureResponse().setHandler(handler->{
			response.complete(handler.result());
		});
	}

}
