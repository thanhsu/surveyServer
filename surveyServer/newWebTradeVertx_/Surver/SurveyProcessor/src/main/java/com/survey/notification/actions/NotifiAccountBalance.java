package com.survey.notification.actions;

import com.survey.constant.UserNotificationEnum;
import com.survey.processor.bean.UserBalanceUpdateBean;
import com.survey.utils.PushMessageBean;

import io.vertx.core.json.JsonObject;

public class NotifiAccountBalance extends BaseSurveyNotification {
	UserBalanceUpdateBean userBalance;

	public NotifiAccountBalance(UserBalanceUpdateBean pUserBalanceUpdate) {
		userBalance = pUserBalanceUpdate;
	}

	@Override
	public void generate() {
		this.setUsername(userBalance.getUsername());
		message = new PushMessageBean();
		message.setData(JsonObject.mapFrom(userBalance));
		message.setType(UserNotificationEnum.ACCOUNTBALANCE);
		message.setDescription(UserNotificationEnum.ACCOUNTBALANCE.getDescription());
		this.setPublic(false);
		this.setPrivate(true);
		this.doSend();
	}

}
