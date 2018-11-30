package com.survey.etheaction;

import com.survey.constant.EventBusDiscoveryConst;
import com.survey.utils.FieldName;
import com.survey.utils.VertxServiceCenter;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

public class BaseEtheProxyAction {
	protected String action;

	public Future<JsonObject> sendToProxyServer() {
		JsonObject message = new JsonObject().put(FieldName.ACTION, action);
		message.put(FieldName.DATA, JsonObject.mapFrom(this));
		Future<JsonObject> lvProxyResult = Future.future();
		VertxServiceCenter.getInstance().sendNewMessage(EventBusDiscoveryConst.ETHEREUMPROXYDISCOVERY.name(), message,
				lvProxyResult);
		return lvProxyResult;
	}

	public void sendToProxyServer(Future<JsonObject> lvProxyResult) {
		JsonObject message = new JsonObject().put(FieldName.ACTION, action);
		message.put(FieldName.DATA, JsonObject.mapFrom(this));
		VertxServiceCenter.getInstance().sendNewMessage(EventBusDiscoveryConst.ETHEREUMPROXYDISCOVERY.name(), message,
				lvProxyResult);
	}

	public Future<JsonObject> sendToProxyServer(String pAction, JsonObject pRequest) {
		JsonObject message = new JsonObject().put(FieldName.ACTION, pAction).put(FieldName.DATA, pRequest);
		Future<JsonObject> lvProxyResult = Future.future();
		VertxServiceCenter.getInstance().sendNewMessage(EventBusDiscoveryConst.ETHEREUMPROXYDISCOVERY.name(), message,
				lvProxyResult);
		return lvProxyResult;
	}

}
