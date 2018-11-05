package com.survey.server;

import com.survey.constant.AtmosphereAPI;
import com.survey.constant.EventBusDiscoveryConst;
import com.survey.utils.Log;
import com.survey.utils.controller.MicroServiceVerticle;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

public class PushServer extends MicroServiceVerticle {
	@Override
	public void init(Vertx vertx, Context context) {
		super.init(vertx, context);
		AtmosphereAPI.getInstance().setSharedData(vertx.sharedData());
	}

	@Override
	public void start() throws Exception {
		super.start();

		vertx.eventBus().<JsonObject>consumer(EventBusDiscoveryConst.SURVEYPUSHSERVERDISCOVEY.value(), message -> {
			JsonObject body = message.body();
			message.reply(new JsonObject().put("success", AtmosphereAPI.getInstance().pushTopicWatchList("survey",
					body.getString("action"), body.getString("session"), body)));
		});

		this.publishEventBusService(EventBusDiscoveryConst.SURVEYPUSHSERVERDISCOVEY.name(),
				EventBusDiscoveryConst.SURVEYPUSHSERVERDISCOVEY.value(), completionHandler -> {
					if (completionHandler.succeeded()) {
						Log.print("Pushlish SURVEYPUSHSERVERDISCOVEY success!");
					} else {
						Log.print("Pushlish SURVEYPUSHSERVERDISCOVEY fail! cause: "
								+ completionHandler.cause().getMessage());
					}
				});
	}
}
