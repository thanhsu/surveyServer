package com.survey.server;

import com.survey.constant.AtmosphereAPI;
import com.survey.constant.EventBusDiscoveryConst;
import com.survey.utils.FieldName;
import com.survey.utils.Log;
import com.survey.utils.controller.MicroServiceVerticle;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

public class PushServer extends MicroServiceVerticle {
	@Override
	public void init(Vertx vertx, Context context) {
		super.init(vertx, context);
		AtmosphereAPI.getInstance().setSharedData(vertx.sharedData());
	}

	@Override
	public void start(Future<Void> startFuture) throws Exception {
		super.start(startFuture);
		vertx.eventBus().<JsonObject>consumer(EventBusDiscoveryConst.SURVEYPUSHPUBLICSERVERDISCOVEY.value(),
				message -> {
					JsonObject body = message.body();
					message.reply(new JsonObject().put("success",
							AtmosphereAPI.getInstance().pushTopicNotification("survey", "notification", "all", body)));
				});

		this.publishEventBusService(EventBusDiscoveryConst.SURVEYPUSHPUBLICSERVERDISCOVEY.name(),
				EventBusDiscoveryConst.SURVEYPUSHPUBLICSERVERDISCOVEY.value(), completionHandler -> {
					if (completionHandler.succeeded()) {
						Log.print("Pushlish SURVEYPUSHPRIVATESERVERDISCOVEY success!");
					} else {
						Log.print("Pushlish SURVEYPUSHPRIVATESERVERDISCOVEY fail! cause: "
								+ completionHandler.cause().getMessage());
					}
				});
		vertx.eventBus().<JsonObject>consumer(EventBusDiscoveryConst.SURVEYPUSHPRIVATESERVERDISCOVEY.value(),
				message -> {
					JsonObject body = message.body();
					message.reply(new JsonObject().put("success", AtmosphereAPI.getInstance().pushTopicToUser("survey",
							body.getString("action"), body.getString("session"), body.getJsonObject(FieldName.DATA))));
				});

		this.publishEventBusService(EventBusDiscoveryConst.SURVEYPUSHPRIVATESERVERDISCOVEY.name(),
				EventBusDiscoveryConst.SURVEYPUSHPRIVATESERVERDISCOVEY.value(), completionHandler -> {
					if (completionHandler.succeeded()) {
						Log.print("Pushlish SURVEYPUSHPRIVATESERVERDISCOVEY success!");
					} else {
						Log.print("Pushlish SURVEYPUSHPRIVATESERVERDISCOVEY fail! cause: "
								+ completionHandler.cause().getMessage());
					}
				});
	}

}
