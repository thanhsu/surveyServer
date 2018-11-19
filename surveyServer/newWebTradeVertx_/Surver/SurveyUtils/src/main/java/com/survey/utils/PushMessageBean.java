package com.survey.utils;

import java.io.Serializable;

import com.survey.constant.UserNotificationEnum;

import io.vertx.core.json.JsonObject;

public class PushMessageBean implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6165922369727330780L;
	private UserNotificationEnum type;
	private String description;
	private JsonObject data;

	public UserNotificationEnum getType() {
		return type;
	}

	public void setType(UserNotificationEnum type) {
		this.type = type;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public JsonObject getData() {
		return data;
	}

	public void setData(JsonObject data) {
		this.data = data;
	}

}
