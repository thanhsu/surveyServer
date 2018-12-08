package com.survey.notification.actions;

import com.survey.constant.UserNotificationEnum;
import com.survey.dbservice.dao.CardDao;
import com.survey.utils.FieldName;
import com.survey.utils.PushMessageBean;

import io.vertx.core.json.JsonObject;

public class NotifiBuyCard extends BaseSurveyNotification {
	String cardID;
	boolean success;

	public NotifiBuyCard(String pCardId, boolean pSuccess) {
		this.setCardID(pCardId);
		this.setSuccess(pSuccess);
	}

	@Override
	public void generate() {
		CardDao lvCardDao = new CardDao();
		if (success) {
			lvCardDao.retrieveCardDetail(cardID).setHandler(handler -> {
				JsonObject dt = new JsonObject();
				if (handler.result() != null) {
					dt.put(FieldName.SUCCESS, true);
					dt = handler.result();
				} else {
					dt.put(FieldName.SUCCESS, false);
				}
				message = new PushMessageBean();
				message.setType(UserNotificationEnum.BUYCARD);
				message.setData(dt);
				message.setDescription(UserNotificationEnum.BUYCARD.getDescription());
				this.doSendPrivate();
			});
		} else {
			lvCardDao.revertThisCard(cardID);
			message = new PushMessageBean();
			message.setType(UserNotificationEnum.BUYCARD);
			message.setData(new JsonObject().put(FieldName.SUCCESS, false));
			message.setDescription(UserNotificationEnum.BUYCARD.getDescription());
			this.doSendPrivate();
		}

	}

	public String getCardID() {
		return cardID;
	}

	public void setCardID(String cardID) {
		this.cardID = cardID;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

}
