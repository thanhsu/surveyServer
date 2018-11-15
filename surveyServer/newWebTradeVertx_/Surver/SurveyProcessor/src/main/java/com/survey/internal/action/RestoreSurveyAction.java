package com.survey.internal.action;

import com.survey.dbservice.dao.SurveyDao;
import com.survey.utils.FieldName;

public class RestoreSurveyAction extends InternalSurveyBaseAction {

	@Override
	public void doProccess() {
		String username = getMessageBody().getString(FieldName.USERNAME);
		String surveyId = getMessageBody().getString(FieldName.SURVEYID);

		SurveyDao lvDao = new SurveyDao();
		lvDao.restoreDisableSurvey(surveyId, username);
		lvDao.getMvFutureResponse().setHandler(handler -> {
			response.complete(handler.result());
		});

	}

}
