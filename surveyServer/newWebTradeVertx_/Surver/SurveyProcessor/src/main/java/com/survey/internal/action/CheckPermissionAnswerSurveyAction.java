package com.survey.internal.action;

import com.survey.dbservice.dao.SurveyDao;
import com.survey.utils.FieldName;

public class CheckPermissionAnswerSurveyAction extends InternalSurveyBaseAction {
	@Override
	public void doProccess() {
		String username = getMessageBody().getString(FieldName.USERNAME);
		String surveyID = getMessageBody().getString(FieldName.SURVEYID);
		// Check point of survey remain

		SurveyDao lvDao = new SurveyDao();
		lvDao.CheckPermisstionDoing(username, surveyID);
		lvDao.getMvFutureResponse().setHandler(handler->{
			response.complete(handler.result());
		});
	}
}
