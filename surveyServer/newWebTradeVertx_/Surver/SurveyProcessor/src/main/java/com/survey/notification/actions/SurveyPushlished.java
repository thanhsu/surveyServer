package com.survey.notification.actions;

import com.survey.utils.PushMessageBean;

public class SurveyPushlished extends BaseSurveyNotification {

	public SurveyPushlished(String username, PushMessageBean pPushMessageBean) {
		super(username, pPushMessageBean);
	}

	@Override
	public void generate() {
		this.setPrivate(true);
		this.setPublic(true);
	}

}
