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
import com.survey.utils.controller.MicroServiceVerticle;

import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

public class InternalProcessorVerticle extends MicroServiceVerticle {
	private static final Map<String, InternalSurveyBaseAction> mvActionMapping = new HashMap<>();

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
					InternalSurveyBaseAction lvAction = mvActionMapping.get(action);
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
		mvActionMapping.put("activeaccount", new ActiveUserAction());
		mvActionMapping.put("changepassword", new ChangePasswordAction());
		mvActionMapping.put("resetpassword", new ResetPasswordAction());
		mvActionMapping.put("userinfo", new RetrieveUserinfoAction());
		mvActionMapping.put("updateuserinfo", new UpdateUserInfoAction());

		// 2
		mvActionMapping.put("retrieveconfig", new RetrieveConfigAction());
		// 3
		mvActionMapping.put("accountbalance", new AccountBalanceProxyAction());
		mvActionMapping.put("pointvalue", new RetrievePointValueAction());
		mvActionMapping.put("cashmethod", new RetrieveCashMethodAction());
		//
		mvActionMapping.put("deposit", new DepositAction());
		mvActionMapping.put("withdraw",new WithdrawAction());
		mvActionMapping.put("cashenquiry", new CashEnquiryAction());
		mvActionMapping.put("cancelcash", new CancelCashAction());
		mvActionMapping.put("payment", new PaymentAction());
		
	}
}
