package com.survey;

import java.util.HashMap;
import java.util.Map;

import com.survey.constant.EventBusDiscoveryConst;
import com.survey.internal.action.BaseSurveyInternalAction;
import com.survey.internal.action.LoginAction;
import com.survey.internal.action.RegisterAction;
import com.survey.utils.MessageDefault;
import com.survey.utils.controller.MicroServiceVerticle;

import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

public class InternalProcessorVerticle extends MicroServiceVerticle {
	private static final Map<String, BaseSurveyInternalAction> mvActionMapping = new HashMap<>();

	@Override
	public void init(Vertx vertx, Context context) {
		super.init(vertx, context);
		initActionMapping();
	}

	@Override
	public void start(Future<Void> startFuture) throws Exception {
		super.start(startFuture);
		vertx.eventBus().<JsonObject>consumer(EventBusDiscoveryConst.SURVEYINTERNALPROCESSORTDISCOVERY.value())
				.handler(handler -> {
					JsonObject lvBody = handler.body();
					String action = lvBody.getString("action");
					BaseSurveyInternalAction lvAction = mvActionMapping.get(action);
					if (lvAction != null) {
						lvAction.init(handler);
						lvAction.doSetResponseHandler();
						lvAction.doProccess();
						// lvAction.doResponse();
					} else {
						handler.reply(MessageDefault.ActionNotFound());
					}
				});
		publishEventBusService(EventBusDiscoveryConst.SURVEYINTERNALPROCESSORTDISCOVERY.toString(),
				EventBusDiscoveryConst.SURVEYINTERNALPROCESSORTDISCOVERY.value(), completionHandler -> {

				});
	}

	private void initActionMapping() {
		mvActionMapping.put("login", new LoginAction());
		mvActionMapping.put("register", new RegisterAction());
	}
}
