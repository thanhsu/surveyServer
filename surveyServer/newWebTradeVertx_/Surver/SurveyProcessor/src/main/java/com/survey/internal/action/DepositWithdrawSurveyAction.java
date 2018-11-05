package com.survey.internal.action;

import com.survey.constant.EventBusDiscoveryConst;
import com.survey.dbservice.dao.ProxyLogDao;
import com.survey.dbservice.dao.SurveyCashDWDao;
import com.survey.dbservice.dao.SurveyDao;
import com.survey.utils.CodeMapping;
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
		SurveyCashDWDao lvCashDWDao = new SurveyCashDWDao();
		switch (DW) {
		case "D":
			// lvCashDWDao.cashDeposit(surveyID, username, amount, remark, "A", "P");
			// Check user cash balance
			Future<JsonObject> lvResp = Future.future();
			VertxServiceCenter.getInstance().sendNewMessage(EventBusDiscoveryConst.ETHEREUMPROXYDISCOVERY.name(),
					new JsonObject().put(FieldName.ACTION, "accountbalance").put(FieldName.USERNAME, username), lvResp);
			lvResp.setHandler(handler -> {
				if (handler.succeeded() && handler.result() != null) {
					float accountbalance = Float.parseFloat(handler.result().getString("balance"));
					float lvAmountTmp = Float.parseFloat(amount);
					if (lvAmountTmp > accountbalance) {
						lvCashDWDao.cashDeposit(surveyID, username, amount, remark, "A", "P").setHandler(saveData -> {
							String tranID = saveData.result();
							Future<JsonObject> lvResp1 = Future.future();
							this.CompleteGenerateResponse(CodeMapping.C0000.toString(), "Deposit success",
									new JsonObject().put(FieldName.SUCCESS, true).put(FieldName.TRANID, tranID),
									response);
							VertxServiceCenter.getInstance().sendNewMessage(
									EventBusDiscoveryConst.ETHEREUMPROXYDISCOVERY.name(),
									new JsonObject().put(FieldName.ACTION, "deposit").put(FieldName.USERNAME, username)
											.put(FieldName.SURVEYID, surveyID).put(FieldName.AMOUNT, amount)
											.put(FieldName.TRANID, tranID),
									lvResp1);
							lvResp1.setHandler(handler1 -> {
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
			Future<JsonObject> lvResp1 = Future.future();
			VertxServiceCenter.getInstance().sendNewMessage(EventBusDiscoveryConst.ETHEREUMPROXYDISCOVERY.name(),
					new JsonObject().put(FieldName.ACTION, "surveybalance").put(FieldName.USERNAME, username), lvResp1);

			lvResp1.setHandler(handler -> {
				if (handler.succeeded() && handler.result() != null) {
					float surveyBalance = Float.parseFloat(handler.result().getString("balance"));
					float lvTmpAmout = Float.parseFloat(amount);
					String lvTmpamount = amount;
					if (lvTmpAmout > surveyBalance) {
						lvTmpamount = String.valueOf(surveyBalance);
					}
					// Change survey status to pending close and send cash withdraw request
					lvCashDWDao.cashWithdraw(surveyID, username, lvTmpamount, remark, "A", "P").setHandler(handler2 -> {
						String tranID = handler2.result();
						Future<JsonObject> lvResp2 = Future.future();
						this.CompleteGenerateResponse(CodeMapping.C0000.toString(), "Withdraw success",
								new JsonObject().put(FieldName.SUCCESS, true).put(FieldName.TRANID, tranID), response);
						VertxServiceCenter.getInstance().sendNewMessage(
								EventBusDiscoveryConst.ETHEREUMPROXYDISCOVERY.name(),
								new JsonObject().put(FieldName.ACTION, "withdraw").put(FieldName.USERNAME, username)
										.put(FieldName.SURVEYID, surveyID).put(FieldName.AMOUNT, amount)
										.put(FieldName.TRANID, tranID),
								lvResp2);
						lvResp2.setHandler(handler1 -> {
							ProxyLogDao lvDao = new ProxyLogDao();
							lvDao.storeNewRequest("withdraw",
									new JsonObject().put(FieldName.ACTION, "withdraw").put(FieldName.USERNAME, username)
											.put(FieldName.SURVEYID, surveyID).put(FieldName.AMOUNT, amount)
											.put(FieldName.TRANID, tranID),
									handler1.result());
						});
						SurveyDao lvSurveyDao = new SurveyDao();
						lvSurveyDao.closesurvey(username, getMessageBody().getString(FieldName.USERID), surveyID, true,
								remark);

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
