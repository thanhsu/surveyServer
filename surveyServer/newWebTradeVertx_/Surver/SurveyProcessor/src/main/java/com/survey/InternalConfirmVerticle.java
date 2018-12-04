package com.survey;

import com.survey.confirm.actions.BaseConfirmAction;
import com.survey.constant.EventBusDiscoveryConst;
import com.survey.utils.Log;
import com.survey.utils.SurveyProcessConstant;
import com.survey.utils.controller.MicroServiceVerticle;

import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;

public class InternalConfirmVerticle extends MicroServiceVerticle {

	@Override
	public void init(Vertx vertx, Context context) {
		super.init(vertx, context);
	}

	@Override
	public void start(Future<Void> startFuture) throws Exception {
		super.start(startFuture);

		vertx.eventBus().<JsonObject>consumer(EventBusDiscoveryConst.SURVEYCONFIRMPROCESSORDISCOVERY.value(), r -> {
			System.out.println("Received confirm message: " + Json.encode(r.body()));
			Log.print("Received confirm message: " + Json.encode(r.body()));
			String action = r.body().getString("action");

			BaseConfirmAction lvAction = SurveyProcessConstant.getInstance().getConfirmAction(action);
			r.reply("OK");
			if (lvAction != null) {
				lvAction.setMessage(r.body());
				lvAction.doProcess(r.body());
			}
		});
		publishEventBusService(EventBusDiscoveryConst.SURVEYCONFIRMPROCESSORDISCOVERY.name(),
				EventBusDiscoveryConst.SURVEYCONFIRMPROCESSORDISCOVERY.value(), completionHandler -> {
					if (completionHandler.succeeded()) {
						System.out.println("Pushlish Eventbus discovey SurveyConfirmAction Success");
					} else {

					}
				});
	}
}
