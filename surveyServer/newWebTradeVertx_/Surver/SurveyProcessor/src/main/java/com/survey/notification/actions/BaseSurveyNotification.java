package com.survey.notification.actions;

import com.survey.dbservice.dao.UserNotificationDao;
import com.survey.utils.Log;
import com.survey.utils.PushMessageBean;
import com.survey.utils.PushServerSender;

import io.vertx.core.json.JsonObject;

public abstract class BaseSurveyNotification implements ISurveyNotification {
	private String username;
	protected PushMessageBean message;
	private UserNotificationDao mvUserNotificationDao;
	private boolean isPublic = false;
	private boolean isPrivate = false;

	public BaseSurveyNotification() {

	}

	public BaseSurveyNotification(String username, PushMessageBean pPushMessageBean) {
		this.setUsername(username);
		this.setMessage(pPushMessageBean);
	}

	public void doSend() {
		if (isPrivate && username != null) {
			// Send to Private and store if send fail
			PushServerSender.sendMessageByPushServer(getUsername(), JsonObject.mapFrom(message)).setHandler(handler -> {
				if (handler.succeeded() && handler.result()) {
					//
					Log.print("Send message private to user " + username + " success");
				} else {
					mvUserNotificationDao = new UserNotificationDao();
					mvUserNotificationDao.storeNewNotification(username, message.getData(), message.getType());
				}
			});
		}

		if (isPublic) {
			// Send to all session
			PushServerSender.sendMessagePublicByPushServer(JsonObject.mapFrom(message));
		}
	}

	protected void doSendPublic() {
		if (isPublic) {
			// Send to all session
			PushServerSender.sendMessagePublicByPushServer(JsonObject.mapFrom(message));
		}
	}

	protected void doSendPrivate() {
		if (isPrivate && username != null) {
			// Send to Private and store if send fail
			PushServerSender.sendMessageByPushServer(getUsername(), JsonObject.mapFrom(message)).setHandler(handler -> {
				if (handler.succeeded() && handler.result()) {
					//
					Log.print("Send message private to user " + username + " success");
				} else {
					mvUserNotificationDao = new UserNotificationDao();
					mvUserNotificationDao.storeNewNotification(username, message.getData(), message.getType());
				}
			});
		}
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public PushMessageBean getMessage() {
		return message;
	}

	public void setMessage(PushMessageBean message) {
		this.message = message;
	}

	public UserNotificationDao getMvUserNotificationDao() {
		return mvUserNotificationDao;
	}

	public void setMvUserNotificationDao(UserNotificationDao mvUserNotificationDao) {
		this.mvUserNotificationDao = mvUserNotificationDao;
	}

	public boolean isPublic() {
		return isPublic;
	}

	public void setPublic(boolean isPublish) {
		this.isPublic = isPublish;
	}

	public boolean isPrivate() {
		return isPrivate;
	}

	public void setPrivate(boolean isPrivate) {
		this.isPrivate = isPrivate;
	}
}
