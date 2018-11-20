package com.survey;

import com.survey.constant.EventBusDiscoveryConst;
import com.survey.internal.action.InternalSurveyBaseAction;
import com.survey.utils.MessageDefault;
import com.survey.utils.SurveyProcessConstant;
import com.survey.utils.controller.MicroServiceVerticle;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

public class InternalProcessorVerticle extends MicroServiceVerticle {

	@Override
	public void init(Vertx vertx, Context context) {
		super.init(vertx, context);
		SurveyProcessConstant.getInstance();
	}

	@Override
	public void start(Future<Void> startFuture) throws Exception {
		super.start(startFuture);
		vertx.eventBus().<JsonObject>consumer(EventBusDiscoveryConst.SURVEYINTERNALPROCESSORTDISCOVERY.value())
				.handler(handler -> {
					JsonObject lvBody = handler.body();
					String action = lvBody.getString("action");
					InternalSurveyBaseAction lvAction = SurveyProcessConstant.getInstance().getInternalAction(action);
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

}
