package com.survey.notification.actions;

import com.survey.constant.UserNotificationEnum;
import com.survey.dbservice.dao.SurveyDao;
import com.survey.utils.FieldName;
import com.survey.utils.PushMessageBean;

public class NotifiNewSurveyResponse extends BaseSurveyNotification {
	private String surveyID;

	public NotifiNewSurveyResponse(String pId) {
		this.setSurveyID(surveyID);
	}

	@Override
	public void generate() {
		this.setPublic(false);
		this.setPrivate(true);
		SurveyDao lvSurveyDao = new SurveyDao();
		lvSurveyDao.retrieveSurveyStatus(getSurveyID()).setHandler(handler -> {
			if (handler.succeeded()) {
				message = new PushMessageBean();
				message.setData(handler.result());
				this.setUsername(handler.result().getString(FieldName.USERNAME));
				handler.result().remove(FieldName.USERNAME);
				message.setType(UserNotificationEnum.SURVEYNEWRESPONSE);
				message.setDescription(UserNotificationEnum.SURVEYNEWRESPONSE.getDescription());
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
