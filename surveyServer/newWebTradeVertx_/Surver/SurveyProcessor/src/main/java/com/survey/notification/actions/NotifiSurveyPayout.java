package com.survey.notification.actions;

import com.survey.constant.UserNotificationEnum;
import com.survey.utils.FieldName;
import com.survey.utils.PushMessageBean;

import io.vertx.core.json.JsonObject;

public class NotifiSurveyPayout extends BaseSurveyNotification {
	private String title;
	private String tranID;
	private double amount;
	@Override
	public void generate() {
		JsonObject msg = new  JsonObject();
		msg.put(FieldName.TITLE, title).put(FieldName.TRANID, tranID).put(FieldName.DW, "D").put(FieldName.AMOUNT,amount);
		message = new PushMessageBean();
		message.setData(msg);
		message.setType(UserNotificationEnum.ANSWERSURVEYPAYOUT);
		message.setDescription(UserNotificationEnum.ANSWERSURVEYPAYOUT.getDescription());
		this.doSendPrivate();
		
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getTranID() {
		return tranID;
	}
	public void setTranID(String tranID) {
		this.tranID = tranID;
	}
	public double getAmount() {
		return amount;
	}
	public void setAmount(double amount) {
		this.amount = amount;
	}
	

}
