package com.survey.utils;

import com.survey.constant.EventBusDiscoveryConst;
import com.survey.utils.FieldName;
import com.survey.utils.VertxServiceCenter;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.Record;

public class PushServerSender {
	public static Future<Boolean> sendMessageByPushServer(String username, JsonObject message) {
		final JsonObject lvMessage = new JsonObject().put(FieldName.DATA, message).put(FieldName.USERNAME, username);
		Future<Boolean> lvResult = Future.future();
		VertxServiceCenter.getInstance().getDiscovery().getRecord(
				new JsonObject().put("name", EventBusDiscoveryConst.SURVEYPUSHPRIVATESERVERDISCOVEY.toString()),
				rs -> {
					if (rs.succeeded() && rs.result() != null) {
						Record record = rs.result();
						VertxServiceCenter.getInstance().getEventbus()
								.<JsonObject>send(record.getLocation().getString("endpoint"), lvMessage, res -> {
									if (res.succeeded()) {
										JsonObject resp = res.result().body();
										lvResult.complete(resp.getBoolean(FieldName.SUCCESS)==null?false:resp.getBoolean(FieldName.SUCCESS));
									} else {
										lvResult.complete(false);
									}
								});

					} else {
						lvResult.complete(false);
					}
				});
		return lvResult;
	}

	public static Future<Boolean> sendMessagePublicByPushServer(JsonObject message) {
		Future<Boolean> lvResult = Future.future();
		VertxServiceCenter.getInstance().getDiscovery().getRecord(
				new JsonObject().put("name", EventBusDiscoveryConst.SURVEYPUSHPUBLICSERVERDISCOVEY.toString()),
				rs -> {
					if (rs.succeeded() && rs.result() != null) {
						Record record = rs.result();
						VertxServiceCenter.getInstance().getEventbus()
								.<JsonObject>send(record.getLocation().getString("endpoint"), message, res -> {
									if (res.succeeded()) {
										JsonObject resp = res.result().body();
										lvResult.complete(resp.getBoolean(FieldName.SUCCESS));
									} else {
										lvResult.complete(false);
									}
								});

					} else {
						lvResult.complete(false);
					}
				});
		return lvResult;
	}
}
