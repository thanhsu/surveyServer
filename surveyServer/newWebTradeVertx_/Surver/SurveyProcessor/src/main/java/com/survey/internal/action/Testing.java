package com.survey.internal.action;

import com.survey.notification.actions.NotifiSurveyPushlished;
import com.survey.utils.FieldName;

public class Testing extends InternalSurveyBaseAction {

	@Override
	public void doProccess() {
		NotifiSurveyPushlished lvPushlished = new NotifiSurveyPushlished(getMessageBody().getString(FieldName.SURVEYID));
		lvPushlished.setPrivate(true);
		lvPushlished.generate();
		
	}

}
