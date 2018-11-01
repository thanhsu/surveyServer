package com.survey.dbservices.action;

import com.survey.utils.MessageDefault;

import io.vertx.core.Future;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

public abstract class BaseDbServiceAction {
	protected Future<JsonObject> mvResponse = Future.future();

	public abstract void doProcess(JsonObject body);

	public void doResponse(Message<JsonObject> handler) {
		mvResponse.setHandler(handlerx -> {
			if (handlerx.succeeded()) {
				handler.reply(handlerx.result());
			} else {
				handler.reply(MessageDefault.RequestFailed(handlerx.cause().getMessage()));
			}
		});
	}

	public Future<JsonObject> getMvResponse() {
		return mvResponse;
	}

	public void setMvResponse(Future<JsonObject> mvResponse) {
		this.mvResponse = mvResponse;
	}
}
