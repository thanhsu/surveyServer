package com.survey.notification.actions;

import com.survey.constant.UserNotificationEnum;
import com.survey.dbservice.dao.SurveyDao;
import com.survey.utils.FieldName;
import com.survey.utils.PushMessageBean;

public class NotifiSurveyStatusUpdate extends BaseSurveyNotification {
	private String surveyID;

	public NotifiSurveyStatusUpdate(String username, PushMessageBean pPushMessageBean) {
		super(username, pPushMessageBean);
	}

	public NotifiSurveyStatusUpdate() {
	}

	@Override
	public void generate() {
		SurveyDao lvSurveyDao = new SurveyDao();
		lvSurveyDao.retrieveSurveyStatus(getSurveyID()).setHandler(handler -> {
			if (handler.succeeded()) {
				message.setData(handler.result());
				this.setUsername(handler.result().getString(FieldName.USERNAME));
				handler.result().remove(FieldName.USERNAME);
				message.setType(UserNotificationEnum.SURVEYSTATE);
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
