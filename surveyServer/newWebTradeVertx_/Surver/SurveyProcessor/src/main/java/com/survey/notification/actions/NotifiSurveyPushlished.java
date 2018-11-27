package com.survey.notification.actions;

import com.survey.constant.UserNotificationEnum;
import com.survey.dbservice.dao.SurveyDao;
import com.survey.utils.FieldName;
import com.survey.utils.PushMessageBean;

public class NotifiSurveyPushlished extends BaseSurveyNotification {
	private String surveyID;

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
				message.setData(handler.result());
				this.setUsername(handler.result().getString(FieldName.USERNAME));
				handler.result().remove(FieldName.USERNAME);
				message.setType(UserNotificationEnum.SURVEYPUSHLISH);
				message.setDescription(UserNotificationEnum.SURVEYPUSHLISH.getDescription());
				this.doSend();
			}
		});
	}

	public String getSurveyID() {
		return surveyID;
	}

	public void setSurveyID(String surveyID) {
		this.surveyID = surveyID;
	}

}
