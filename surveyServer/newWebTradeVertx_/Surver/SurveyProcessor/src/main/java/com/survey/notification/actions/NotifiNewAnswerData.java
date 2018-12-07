package com.survey.notification.actions;

import com.survey.constant.UserNotificationEnum;
import com.survey.dbservice.dao.SurveyDao;
import com.survey.dbservice.dao.SurveySubmitDao;
import com.survey.utils.FieldName;
import com.survey.utils.Log;
import com.survey.utils.PushMessageBean;

import io.vertx.core.json.JsonObject;

public class NotifiNewAnswerData extends BaseSurveyNotification {
	String answerID;
	String tranID;
	double amount;

	@Override
	public void generate() {
		if (getAnswerID() == null) {
			return;
		}
		SurveySubmitDao lvSubmitDao = new SurveySubmitDao();
		lvSubmitDao.retrieveAllAnswer(answerID).setHandler(handler -> {
			if (handler.result() != null) {
				JsonObject answerDt = handler.result();
				SurveyDao lvSurveyDao = new SurveyDao();
				lvSurveyDao.retrieveSurvey(new JsonObject().put(FieldName._ID, answerDt.getString(FieldName.SURVEYID)),
						h -> {
							if (h.result() != null) {
								if (!h.result().isEmpty()) {
									JsonObject msg = new JsonObject();
									String title =h.result().get(0).getString(FieldName.TITLE);
									msg.put(FieldName.TITLE, title);
									msg.put(FieldName.SURVEYID, answerDt.getString(FieldName.SURVEYID));
									msg.put(FieldName.AMOUNT, amount);
									msg.put(FieldName.ANSWERDATA, answerDt.getJsonObject(FieldName.DATA));
									message = new PushMessageBean();
									message.setData(msg);
									message.setType(UserNotificationEnum.SURVEYNEWRESPONSE);
									message.setDescription(UserNotificationEnum.SURVEYNEWRESPONSE.getDescription());
									this.setUsername(h.result().get(0).getString(FieldName.USERNAME));
									this.doSendPrivate();
									//Send to client
									NotifiSurveyPayout lvNotifiSurveyPayout = new NotifiSurveyPayout();
									lvNotifiSurveyPayout.setUsername(answerDt.getString(FieldName.USERNAME));
									lvNotifiSurveyPayout.setTitle(title);
									lvNotifiSurveyPayout.setTranID(getTranID());
									lvNotifiSurveyPayout.setAmount(getAmount());
									lvNotifiSurveyPayout.generate();
								}
							}
							Log.print("Survey Answer id not found for notification, " + getAnswerID(), Log.ERROR_LOG);
						});
			}
		});
	}

	public String getAnswerID() {
		return answerID;
	}

	public void setAnswerID(String answerID) {
		this.answerID = answerID;
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
