package com.survey.admin.actions;

import io.vertx.core.json.JsonObject;

public abstract class BaseAdminAction {
	private JsonObject messageBody;

	public void generateRequest(JsonObject pMessage) {
		this.setMessageBody(pMessage);
	}

	public abstract void doProcess();

	public JsonObject getMessageBody() {
		return messageBody;
	}

	public void setMessageBody(JsonObject messageBody) {
		this.messageBody = messageBody;
	}
}
