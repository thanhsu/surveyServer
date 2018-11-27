package com.survey.notification.actions;

import com.survey.utils.PushMessageBean;

public class NotifiActiveUserSuccess extends BaseSurveyNotification {

	public NotifiActiveUserSuccess(String username, PushMessageBean pPushMessageBean) {
		super(username, pPushMessageBean);

	}

	@Override
	public void generate() {
		this.setPrivate(true);
		this.doSend();
	}

}
