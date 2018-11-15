package com.survey.internal.action;

import com.survey.dbservice.dao.UserNotificationDao;
import com.survey.utils.FieldName;

public class RetrieveUserNotificationAction extends InternalSurveyBaseAction {

	@Override
	public void doProccess() {
		String lvUsername = getMessageBody().getString(FieldName.USERNAME);
		UserNotificationDao lvNotificationDao = new UserNotificationDao();
		lvNotificationDao.retriveNotification(lvUsername);
		lvNotificationDao.getMvFutureResponse().setHandler(handler -> {
			response.complete(handler.result());
		});

	}

}
