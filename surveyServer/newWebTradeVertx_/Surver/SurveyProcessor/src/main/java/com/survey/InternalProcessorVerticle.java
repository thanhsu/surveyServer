package com.survey;

import java.util.HashMap;
import java.util.Map;

import com.survey.constant.EventBusDiscoveryConst;
import com.survey.dbservices.action.UpdateUserInfo;
import com.survey.internal.action.AccountBalanceProxyAction;
import com.survey.internal.action.ActiveUserAction;
import com.survey.internal.action.CancelCashAction;
import com.survey.internal.action.CashEnquiryAction;
import com.survey.internal.action.InternalSurveyBaseAction;
import com.survey.internal.action.ChangePasswordAction;
import com.survey.internal.action.DepositAction;
import com.survey.internal.action.LoginAction;
import com.survey.internal.action.PaymentAction;
import com.survey.internal.action.RegisterAction;
import com.survey.internal.action.ResetPasswordAction;
import com.survey.internal.action.RetrieveCashMethodAction;
import com.survey.internal.action.RetrieveConfigAction;
import com.survey.internal.action.RetrievePointValueAction;
import com.survey.internal.action.RetrieveUserinfoAction;
import com.survey.internal.action.UpdateUserInfoAction;
import com.survey.internal.action.WithdrawAction;
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