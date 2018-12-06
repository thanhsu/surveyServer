package com.survey.internal.action;

import com.survey.constant.EventBusDiscoveryConst;
import com.survey.dbservice.dao.CashDepositDao;
import com.survey.dbservice.dao.CashWithdrawDao;
import com.survey.dbservice.dao.ProxyLogDao;
import com.survey.dbservice.dao.SurveyCashDWDao;
import com.survey.dbservice.dao.SurveyDao;
import com.survey.etheaction.ProxyAccountBalance;
import com.survey.etheaction.ProxySurveyBalance;
import com.survey.etheaction.ProxySurveyDeposit;
import com.survey.etheaction.ProxySurveyWithdraw;
import com.survey.utils.CodeMapping;
import com.survey.utils.ECashDepositType;
import com.survey.utils.FieldName;
import com.survey.utils.MessageDefault;
import com.survey.utils.VertxServiceCenter;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

public class DepositWithdrawSurveyAction extends InternalSurveyBaseAction {

	@Override
	public void doProccess() {
		String surveyID = getMessageBody().getString(FieldName.SURVEYID);
		String username = getMessageBody().getString(FieldName.USERNAME);
		String DW = getMessageBody().getString(FieldName.DW);
		String amount = getMessageBody().getValue(FieldName.AMOUNT).toString();
		String remark = getMessageBody().getString(FieldName.REMARK);

		switch (DW) {
		case "D":
			CashWithdrawDao lvCashDWDao = new CashWithdrawDao();
			// lvCashDWDao.cashDeposit(surveyID, username, amount, remark, "A", "P");
			// Check user cash balance
			Future<JsonObject> lvResp = Future.future();
			ProxyAccountBalance lvProxyAccountBalance = new ProxyAccountBalance(username);
			lvProxyAccountBalance.sendToProxyServer(lvResp);

			lvResp.setHandler(handler -> {
				if (handler.succeeded() && handler.result() != null) {
					if (!handler.result().getString(FieldName.CODE).equals("P0000")) {
						response.complete(handler.result());
						return;
					}
					float accountbalance = Float
							.parseFloat(handler.result().getJsonObject(FieldName.DATA).getString("balance"));
					float lvAmountTmp = Float.parseFloat(amount);
					if (accountbalance > lvAmountTmp) {
						lvCashDWDao.createSurveyCashDeposit(surveyID, username, amount, remark).setHandler(saveData -> {
							String tranID = saveData.result();
							Future<JsonObject> lvResp1 = Future.future();
							this.CompleteGenerateResponse(CodeMapping.C0000.toString(), "Deposit success",
									new JsonObject().put(FieldName.SUCCESS, true).put(FieldName.TRANID, tranID),
									response);

							ProxySurveyDeposit lvProxySurveyDeposit = new ProxySurveyDeposit(surveyID, username, tranID,
									amount);
							lvProxySurveyDeposit.sendToProxyServer(lvResp1);

							lvResp1.setHandler(handler1 -> {
								if (handler1.result() != null) {
									if (!handler1.result().getString(FieldName.CODE).equals("P0000")) {

									}
								}
								ProxyLogDao lvDao = new ProxyLogDao();
								lvDao.storeNewRequest("deposit",
										new JsonObject().put(FieldName.ACTION, "deposit")
												.put(FieldName.USERNAME, username).put(FieldName.SURVEYID, surveyID)
												.put(FieldName.AMOUNT, amount).put(FieldName.TRANID, tranID),
										handler1.result());
							});
						});

					} else {
						this.CompleteGenerateResponse(CodeMapping.S7777.toString(), CodeMapping.S7777.value(),
								getMessageBody().put("accountbalance", accountbalance), response);
					}
				} else {
					this.CompleteGenerateResponse(CodeMapping.C1111.toString(), "Can not get account info now!", null,
							response);
				}
			});

			break;
		case "W":
			// Check survey balance
			CashDepositDao lvCashWDao = new CashDepositDao();
			Future<JsonObject> lvResp1 = Future.future();
			ProxySurveyBalance lvProxySurveyBalance = new ProxySurveyBalance(surveyID);
			lvProxySurveyBalance.sendToProxyServer(lvResp1);

			lvResp1.setHandler(handler -> {
				if (handler.result() != null) {
					if (!handler.result().getString(FieldName.CODE).equals("P0000")) {
						response.complete(handler.result());
						return;
					}
					double surveyBalance = Double
							.parseDouble(handler.result().getJsonObject(FieldName.DATA).getString("balance"));
					double lvTmpAmout = Double.parseDouble(amount);
					boolean isStop = true;
					if (lvTmpAmout > surveyBalance) {
						this.CompleteGenerateResponse(CodeMapping.S0007.toString(), CodeMapping.S0007.value(),
								handler.result().getJsonObject(FieldName.DATA), response);
						return;

					}
					// Change survey status to pending close and send cash withdraw request
					lvCashWDao.createSurveyWithdraw(surveyID, username,ECashDepositType.SURVEYWITHDRAW ).setHandler(handler2 -> {
						if (handler2.result() != null) {
							String tranID = handler2.result();
							Future<JsonObject> lvResp2 = Future.future();
							this.CompleteGenerateResponse(CodeMapping.C0000.toString(), "Withdraw success",
									new JsonObject().put(FieldName.SUCCESS, true).put(FieldName.TRANID, tranID),
									response);

							ProxySurveyWithdraw lvProxySurveyWithdraw = new ProxySurveyWithdraw(surveyID, username,
									tranID, amount);
							lvProxySurveyWithdraw.sendToProxyServer(lvResp2);

							lvResp2.setHandler(handler1 -> {
								// TODO if error
								// If success
								if (isStop) {
									SurveyDao lvSurveyDao = new SurveyDao();
									lvSurveyDao.closesurvey(username, surveyID, true, remark);
								}
								ProxyLogDao lvDao = new ProxyLogDao();
								lvDao.storeNewRequest("withdraw",
										new JsonObject().put(FieldName.ACTION, "withdraw")
												.put(FieldName.USERNAME, username).put(FieldName.SURVEYID, surveyID)
												.put(FieldName.AMOUNT, amount).put(FieldName.TRANID, tranID),
										handler1.result());
							});
						} else {
							this.CompleteGenerateResponse(CodeMapping.S0004.name(), CodeMapping.S0004.value(),null , response);
						}
					});

				} else {
					this.CompleteGenerateResponse(CodeMapping.C1111.toString(), "Can not get survey info now!", null,
							response);
				}
			});
			break;

		default:
			response.complete(MessageDefault.ParamError("Non support DW type: " + DW));
			break;
		}
	}

}
