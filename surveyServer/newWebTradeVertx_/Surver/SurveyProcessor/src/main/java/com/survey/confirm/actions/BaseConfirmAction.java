package com.survey.confirm.actions;

import io.vertx.core.json.JsonObject;

public abstract class BaseConfirmAction {
	public JsonObject messsage;

	public void setMessage(JsonObject message) {
		this.messsage = message;
	}

	public abstract void doProcess(JsonObject msg);
}
