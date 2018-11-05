package com.survey.internal.action;

import com.survey.utils.FieldName;

public class CloseAndStopSurveyAction extends InternalSurveyBaseAction {

	@Override
	public void doProccess() {
		String username = getMessageBody().getString(FieldName.USERNAME);
		String userid = getMessageBody().getString(FieldName.USERID);
		String surveyID = getMessageBody().getString(FieldName.SURVEYID);
		boolean isStop = getMessageBody().getBoolean(FieldName.ISSTOP) == null ? false
				: getMessageBody().getBoolean(FieldName.ISSTOP);
		String remark = getMessageBody().getString(FieldName.REMARK);
		
	}

}
