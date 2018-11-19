package com.survey.dbservice.dao;

import java.util.Date;

import com.survey.constant.UserNotificationEnum;
import com.survey.utils.CodeMapping;
import com.survey.utils.FieldName;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class UserNotificationDao extends SurveyBaseDao {
	public static final String collectionUserNotification = "usernotification";

	public UserNotificationDao() {
		super();
		this.setCollectionName(collectionUserNotification);
	}

	public void storeNewNotification(String username, JsonObject notification, UserNotificationEnum type) {
		this.saveDocument(new JsonObject().put(FieldName.USERNAME, username).put(FieldName.DATA, notification)
				.put(FieldName.TYPE, type.name()).put(FieldName.INPUTTIME, new Date().getTime()));
	}

	public void retriveNotification(String username) {
		this.queryDocument(new JsonObject().put(FieldName.USERNAME, username), handler -> {
			if (handler.succeeded() && handler.result() != null) {
				this.CompleteGenerateResponse(CodeMapping.C0000.toString(), "", handler.result());
			} else {
				this.CompleteGenerateResponse(CodeMapping.C1111.toString(), "Not found new Notification", null);
			}
		});
	}

	public void deleteNotification(String username, JsonArray id) {
		id.forEach(idNotifi -> {
			this.delteDocument(new JsonObject().put(FieldName._ID, idNotifi).put(FieldName.USERNAME, username),
					handler -> {
					});
		});
	}
}
