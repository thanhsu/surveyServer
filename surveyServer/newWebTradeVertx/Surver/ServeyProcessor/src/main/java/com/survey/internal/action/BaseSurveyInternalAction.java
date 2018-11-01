package com.survey.internal.action;

import com.survey.constant.HttpParameter;
import com.survey.utils.MessageDefault;

import io.vertx.core.Future;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

public abstract class BaseSurveyInternalAction {
	protected JsonObject messageBody;
	protected String loginID;
	protected String service;
	RoutingContext routingContext;
	protected JsonObject messageResponse;
	protected Message<JsonObject> eventBusRequest;
	protected Future<JsonObject> response = Future.future();

	public BaseSurveyInternalAction() {

	}

	public void init(Message<JsonObject> pEventMsg) {
		messageBody = pEventMsg.body();
		loginID = messageBody.getString(HttpParameter.LOGINID);
		messageResponse = new JsonObject();
		eventBusRequest = pEventMsg;
		response = Future.future();
	}

	public BaseSurveyInternalAction(RoutingContext rtx) {
		this.setRoutingContext(rtx);
		if (rtx.request().method() == HttpMethod.POST) {
			this.setMessageBody(rtx.getBodyAsJson());
		}
	}

	public abstract void doProccess();

	public void doSetResponseHandler() {
		response.setHandler(handler -> {
			if (handler.succeeded() && handler.result() != null) {
				eventBusRequest.reply(handler.result());
			} else {
				eventBusRequest.reply(MessageDefault.RequestFailed(handler.cause().getMessage()));
			}
		});
	}

	public JsonObject getMessageBody() {
		return messageBody;
	}

	public void setMessageBody(JsonObject messageBody) {
		this.messageBody = messageBody;
	}

	public String getLoginID() {
		return loginID;
	}

	public void setLoginID(String loginID) {
		this.loginID = loginID;
	}

	public String getService() {
		return service;
	}

	public void setService(String service) {
		this.service = service;
	}

	public RoutingContext getRoutingContext() {
		return routingContext;
	}

	public void setRoutingContext(RoutingContext routingContext) {
		this.routingContext = routingContext;
	}
}
