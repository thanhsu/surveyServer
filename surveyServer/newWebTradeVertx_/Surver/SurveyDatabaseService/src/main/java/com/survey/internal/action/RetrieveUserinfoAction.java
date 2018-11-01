package com.survey.internal.action;

import com.survey.dbservice.dao.UserDao;
import com.survey.utils.FieldName;
import com.survey.utils.MessageDefault;

public class RetrieveUserinfoAction extends InternalSurveyBaseAction {

	@Override
	public void doProccess() {
		String username = getMessageBody().getString(FieldName.USERNAME);
		UserDao lvDao = new UserDao();
		lvDao.doGetUserInfobyUserName(username);
		lvDao.getMvFutureResponse().setHandler(handler -> {
			if (handler.succeeded()) {
				response.complete(handler.result());
			} else {
				response.complete(MessageDefault.RequestFailed(handler.cause().getMessage()));
			}
		});
	}

}
