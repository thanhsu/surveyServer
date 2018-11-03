package com.survey.dbservices;

import com.survey.constant.EventBusDiscoveryConst;
import com.survey.dbservices.action.BaseDbServiceAction;
import com.survey.utils.MessageDefault;
import com.survey.utils.controller.MicroServiceVerticle;

import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;

public class DBService extends MicroServiceVerticle {
	EventBus mvEventBus;

	@Override
	public void init(Vertx vertx, Context context) {
		super.init(vertx, context);
		mvEventBus = vertx.eventBus();
	}

	@Override
	public void start(Future<Void> startFuture) throws Exception {
		super.start(startFuture);
		mvEventBus.<JsonObject>consumer(EventBusDiscoveryConst.SURVEYDBDISCOVERY.value()).handler(handler -> {
			JsonObject lvBody = handler.body();
			String action = lvBody.getString("action");
			if (action != null) {
				BaseDbServiceAction lvAction = DBServiceInit.getMvActionAuthMapping().get(action);
				try {
					lvAction.doProcess(lvBody);
					lvAction.doResponse(handler);
				} catch (Exception e) {
					handler.reply(MessageDefault.ParamError(e.getMessage()));
				}
			} else {
				handler.reply(MessageDefault.ActionNotFound());
			}
		});
		publishEventBusService(EventBusDiscoveryConst.SURVEYDBDISCOVERY.toString(),
				EventBusDiscoveryConst.SURVEYDBDISCOVERY.value(), completionHandler -> {
					
				});
	}

}
