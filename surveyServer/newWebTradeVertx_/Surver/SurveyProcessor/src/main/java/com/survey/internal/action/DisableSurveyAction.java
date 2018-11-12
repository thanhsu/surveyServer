package com.survey.internal.action;

import com.survey.dbservice.dao.SurveyDao;
import com.survey.utils.FieldName;

public class DisableSurveyAction extends InternalSurveyBaseAction {
	// Close and withdraw all survey data
	@Override
	public void doProccess() {
		SurveyDao lvDao = new SurveyDao();
		String username = getMessageBody().getString(FieldName.USERNAME);
		String lvUserid = getMessageBody().getString(FieldName.USERID);
		String surveyID = getMessageBody().getString(FieldName.SURVEYID);
		String remark = getMessageBody().getString(FieldName.REMARK);
		lvDao.closesurvey(username, lvUserid, surveyID, true, remark);
		lvDao.getMvFutureResponse().setHandler(handler -> {
			response.complete(handler.result());
		});
	}

}
