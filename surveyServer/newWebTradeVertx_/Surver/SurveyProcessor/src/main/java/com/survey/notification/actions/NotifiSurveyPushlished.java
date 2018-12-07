package com.survey.notification.actions;

import com.survey.constant.UserNotificationEnum;
import com.survey.dbservice.dao.SurveyDao;
import com.survey.utils.FieldName;
import com.survey.utils.PushMessageBean;

public class NotifiSurveyPushlished extends BaseSurveyNotification {
	private String surveyID;
	private UserNotificationEnum lvNotificationEnum = UserNotificationEnum.SURVEYPUSHLISH;
	private String surveybalance;
	public NotifiSurveyPushlished(String username, PushMessageBean pPushMessageBean) {
		super(username, pPushMessageBean);
	}

	public NotifiSurveyPushlished(String pSurveyID) {
		this.setPublic(true);
		this.setPrivate(true);
		this.setSurveyID(pSurveyID);
	}

	@Override
	public void generate() {
		SurveyDao lvSurveyDao = new SurveyDao();
		lvSurveyDao.retrieveSurveyStatus(getSurveyID()).setHandler(handler -> {
			if (handler.succeeded()) {
				message = new PushMessageBean();
				this.setUsername(handler.result().getString(FieldName.USERNAME));
				handler.result().remove(FieldName.USERNAME);
				handler.result().put(FieldName.SURVEYBALANCE, getSurveybalance());
				message.setData(handler.result());
				
				
				message.setType(lvNotificationEnum);
				message.setDescription(UserNotificationEnum.SURVEYPUSHLISH.getDescription());
				this.doSendPrivate();
			}
		});
	}

	public String getSurveyID() {
		return surveyID;
	}

	public void setSurveyID(String surveyID) {
		this.surveyID = surveyID;
	}

	public UserNotificationEnum getLvNotificationEnum() {
		return lvNotificationEnum;
	}

	public void setLvNotificationEnum(UserNotificationEnum lvNotificationEnum) {
		this.lvNotificationEnum = lvNotificationEnum;
	}

	public String getSurveybalance() {
		return surveybalance;
	}

	public void setSurveybalance(String surveybalance) {
		this.surveybalance = surveybalance;
	}

}
