package com.survey.dbservice.dao;

import io.vertx.core.json.JsonObject;

public class UserNotificationDao extends SurveyBaseDao {
	public static final String collectionUserNotification="usernotification";
	
	public UserNotificationDao() {
		super();
		this.setCollectionName(collectionUserNotification);
	}
	
	public void storeNewNotification(String userID, JsonObject notification) {
		this.saveDocument(new JsonObject());
	}
}
