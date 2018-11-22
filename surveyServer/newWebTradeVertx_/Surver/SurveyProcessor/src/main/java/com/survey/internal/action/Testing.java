package com.survey.internal.action;

import com.survey.notification.actions.SurveyPushlished;
import com.survey.utils.FieldName;

public class Testing extends InternalSurveyBaseAction {

	@Override
	public void doProccess() {
		SurveyPushlished lvPushlished = new SurveyPushlished(getMessageBody().getString(FieldName.SURVEYID));
		lvPushlished.setPrivate(true);
		lvPushlished.generate();
		
	}

}
