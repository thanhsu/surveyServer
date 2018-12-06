package com.survey.internal.action;

import com.survey.dbservice.dao.CardDao;
import com.survey.dbservice.dao.CashWithdrawDao;
import com.survey.dbservice.dao.UtilsDao;
import com.survey.etheaction.ProxyAccountBalance;
import com.survey.etheaction.ProxyCashWithdrawWithSystem;
import com.survey.utils.CodeMapping;
import com.survey.utils.FieldName;
import com.survey.utils.MessageDefault;
import com.survey.utils.Utils;

import io.vertx.core.Future;
import io.vertx.core.json.*;;

public class CardDataAction extends InternalSurveyBaseAction {

	@Override
	public void doProccess() {
		String method = getMessageBody().getString(FieldName.METHOD);
		switch (method) {
		case "buy":
			buyCard();
			break;

		default:
			response.complete(MessageDefault.ActionNotFound());
			break;
		}

	}

	private void buyCard() {
		String userName = getMessageBody().getString(FieldName.USERNAME);
		String categoryID = getMessageBody().getString(FieldName.CATEGORYID);
		String strAmount = getMessageBody().getString(FieldName.AMOUNT);
		double amount = Double.parseDouble(getMessageBody().getString(FieldName.AMOUNT));
		UtilsDao lvUtilsDao = new UtilsDao();
		lvUtilsDao.retrieveEthePointValue(false).setHandler(handler -> {
			if (handler.result() != null) {
				double value = Double.parseDouble(handler.result().getValue(FieldName.VALUE).toString());
				int unit = Integer.parseInt(handler.result().getValue(FieldName.UNIT).toString());
				double pointForBuy = (amount / value) * unit;
				ProxyAccountBalance lvProxyAccountBalance = new ProxyAccountBalance(userName);
				lvProxyAccountBalance.sendToProxyServer().setHandler(h2 -> {
					if (h2.result() != null) {
						if (h2.result().getString(FieldName.CODE).equals("P0000")) {
							double balance = Double.parseDouble(
									h2.result().getJsonObject(FieldName.DATA).getValue(FieldName.BALANCE).toString());
							if (pointForBuy > balance) {
								this.CompleteGenerateResponse(CodeMapping.T1111.name(), CodeMapping.T1111.value(),
										h2.result().getJsonObject(FieldName.DATA), response);
								return;
							}
							// Kiem tra tinh hop le va so the con trong
							CardDao lvCardDao = new CardDao();
							Future<JsonObject> lvFuture = Future.future();
							lvCardDao.retrieveCard(userName, categoryID, strAmount, lvFuture);
							if (lvFuture.succeeded() && lvFuture.result() != null) {
								CashWithdrawDao lvCashWithdrawDao = new CashWithdrawDao();
								String cardID = lvFuture.result().getString(FieldName._ID);
								lvCashWithdrawDao.storeNewWithdrawBuyCard(amount, "VND", "Buy Card "+categoryID+" value: "+strAmount, value, lvFuture.result().getString(FieldName._ID)).setHandler(h3->{
									if(h3.result()!=null) {
										ProxyCashWithdrawWithSystem lvCashWithdrawWithSystem = new ProxyCashWithdrawWithSystem();
										lvCashWithdrawWithSystem.setAmount(String.valueOf(pointForBuy));
										lvCashWithdrawWithSystem.setFromuser(userName);
										lvCashWithdrawWithSystem.setTransid(h3.result());
										lvCashWithdrawWithSystem.sendToProxyServer().setHandler(h4->{
											if(h4.result()!=null) {
												if(h4.result().getString(FieldName.CODE).equals("P0000")) {
													this.CompleteGenerateResponse(CodeMapping.CR00.name(), CodeMapping.CR00.value(), "", response);
													return;
												}
											}
											CardDao lvCardDao2 = new CardDao();
											lvCardDao2.revertThisCard(cardID);
											this.CompleteGenerateResponse(CodeMapping.C4444.name(), CodeMapping.C4444.value(), null, response);
										});
									}else {
										CardDao lvCardDao2 = new CardDao();
										lvCardDao2.revertThisCard(cardID);
										this.CompleteGenerateResponse(CodeMapping.C4444.name(), CodeMapping.C4444.value(), null, response);
									}
								});
							} else {
								this.CompleteGenerateResponse(CodeMapping.CR11.name(), CodeMapping.CR11.value(), null,
										response);
							}
							return;
						}
					}
					this.CompleteGenerateResponse(CodeMapping.C4444.name(), CodeMapping.C4444.value(), null, response);

				});
			} else {
				this.CompleteGenerateResponse(CodeMapping.C4444.name(), CodeMapping.C4444.value(), null, response);
			}
		});
		ProxyAccountBalance lvAccountBalance = new ProxyAccountBalance(userName);
	}

}
