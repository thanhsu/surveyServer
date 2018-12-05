package com.survey.internal.action;

import com.survey.constant.EventBusDiscoveryConst;
import com.survey.dbservice.dao.ProxyLogDao;
import com.survey.dbservice.dao.SurveyDao;
import com.survey.etheaction.ProxyAccountBalance;
import com.survey.utils.CodeMapping;
import com.survey.utils.FieldName;
import com.survey.utils.Log;
import com.survey.utils.MessageDefault;
import com.survey.utils.VertxServiceCenter;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

public class PushlishSurveyAction extends InternalSurveyBaseAction {

	@Override
	public void doProccess() {
		String surveyID = getMessageBody().getString(FieldName.SURVEYID);
		String userID = getMessageBody().getString(FieldName.USERID);
		String username = getMessageBody().getString(FieldName.USERNAME);

		double limitResp = getMessageBody().getDouble(FieldName.LIMITRESPONSE);
		float pointPerOne = getMessageBody().getFloat(FieldName.PAYOUT);

		float initialFund = getMessageBody().getFloat(FieldName.INITIALFUND);
		boolean notifi = getMessageBody().getBoolean(FieldName.NOTIFY);
		float limitFund = getMessageBody().getFloat(FieldName.LIMITFUND);

		// Check account balance
		ProxyAccountBalance lvProxyAccountBalance = new ProxyAccountBalance(username);

		Future<JsonObject> lvAccountBalance = Future.future();
		lvProxyAccountBalance.sendToProxyServer(lvAccountBalance);

		/*VertxServiceCenter.getInstance().sendNewMessage(EventBusDiscoveryConst.ETHEREUMPROXYDISCOVERY.name(),
				new JsonObject().put(FieldName.ACTION, "userinfo").put(FieldName.USERNAME, username), lvAccountBalance);*/
		lvAccountBalance.setHandler(handler -> {
			if (handler.succeeded() && handler.result() != null) {
				// Check account balance
				/*
				 * JsonObject accountbalance = new JsonObject().put("success",
				 * "1").put("balance", "10000000") handler.result() ;
				 */
				if (!handler.result().getString(FieldName.CODE).equals("P0000")) {
					this.CompleteGenerateResponse(CodeMapping.P2222.toString(), CodeMapping.P2222.value(),
							handler.result(), response);
					return;
				}
				JsonObject accountbalance = handler.result().getJsonObject(FieldName.DATA);
				if (accountbalance.getValue(FieldName.BALANCE)!=null) {
					float balance = Float.parseFloat(accountbalance.getString(FieldName.BALANCE));
					if (balance < initialFund) {
						this.CompleteGenerateResponse(CodeMapping.S7777.toString(), CodeMapping.S7777.value(),
								getMessageBody().put("accountbalance", accountbalance), response);
					} else {
						SurveyDao lvSurveyDao1 = new SurveyDao();
						lvSurveyDao1.pushlishSurvey(surveyID, username, limitResp, pointPerOne, initialFund, notifi,
								limitFund);
						lvSurveyDao1.getMvFutureResponse().setHandler(create -> {
							if (create.result().getString(FieldName.CODE).equals(CodeMapping.C0000.toString())) {
								response.complete(create.result());
								// Send to servey
								/*
								 * Future<JsonObject> lvPushlish = Future.future();
								 * VertxServiceCenter.getInstance().sendNewMessage(
								 * EventBusDiscoveryConst.ETHEREUMPROXYDISCOVERY.name(), getMessageBody(),
								 * lvPushlish); lvPushlish.setHandler(x -> { if (x.succeeded()) { ProxyLogDao lv
								 * = new ProxyLogDao(); lv.storeNewRequest("pushlish", getMessageBody(),
								 * x.result()); } else {
								 * Log.print("Cannot Proxy action pushlish survey to ethereum survey!Cause: " +
								 * x.cause().getMessage()); } });
								 */
							} else {
								response.complete(create.result());
							}
						});
					}
				} else {
					response.complete(MessageDefault.PermissionError());
				}

			} else {
				response.complete(MessageDefault.RequestFailed("Cannot check account balance now!"));
			}
		});

	}

}
