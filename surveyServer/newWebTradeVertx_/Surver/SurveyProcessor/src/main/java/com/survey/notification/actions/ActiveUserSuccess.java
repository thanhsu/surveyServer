package com.survey.notification.actions;

import com.survey.utils.PushMessageBean;

public class ActiveUserSuccess extends BaseSurveyNotification {

	public ActiveUserSuccess(String username, PushMessageBean pPushMessageBean) {
		super(username, pPushMessageBean);

	}

	@Override
	public void generate() {
		this.setPrivate(true);
		this.doSend();
	}

}
