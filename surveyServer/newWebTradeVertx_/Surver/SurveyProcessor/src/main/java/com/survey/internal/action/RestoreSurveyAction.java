package com.survey.internal.action;

import com.survey.utils.FieldName;

public class RestoreSurveyAction extends InternalSurveyBaseAction {

	@Override
	public void doProccess() {
		String username=  getMessageBody().getString(FieldName.USERNAME);
		String surveyId = getMessageBody().getString(FieldName.SURVEYID);
		
		
		
	}

}
