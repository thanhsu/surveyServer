package com.survey.internal.action;

import com.survey.dbservice.dao.CardDao;
import com.survey.dbservice.dao.CashWithdrawDao;
import com.survey.dbservice.dao.UtilsDao;
import com.survey.etheaction.ProxyAccountBalance;
import com.survey.etheaction.ProxyCashWithdrawWithSystem;
import com.survey.utils.CodeMapping;
import com.survey.utils.ECashWithdrawType;
import com.survey.utils.FieldName;
import com.survey.utils.MessageDefault;

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
		case "new":
			newCard();
			break;
		case "disable":
			disableCard();
			break;
		case "all":
			retriveALl();
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
				double value = Double.parseDouble(handler.result().getJsonArray(FieldName.DATA).getJsonObject(0)
						.getValue(FieldName.VALUE).toString());
				double unit = Double.parseDouble(handler.result().getJsonArray(FieldName.DATA).getJsonObject(0)
						.getValue(FieldName.UNIT).toString());
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
							Future<JsonObject> lvFuture;
							lvFuture = lvCardDao.retrieveCard(userName, categoryID, strAmount);
							lvFuture.setHandler(h3 -> {
								if (h3.result() != null) {
									CashWithdrawDao lvCashWithdrawDao = new CashWithdrawDao();
									String cardID = h3.result().getString(FieldName._ID);
									lvCashWithdrawDao.storeNewWithdrawBuyCard(amount, "VND",
											"Buy Card " + categoryID + " value: " + strAmount, value,
											h3.result().getString(FieldName._ID)).setHandler(h4 -> {
												if (h4.result() != null) {
													ProxyCashWithdrawWithSystem lvCashWithdrawWithSystem = new ProxyCashWithdrawWithSystem();
													lvCashWithdrawWithSystem.setAmount(String.valueOf(pointForBuy));
													lvCashWithdrawWithSystem.setFromuser(userName);
													lvCashWithdrawWithSystem.setTransid(h4.result());
													lvCashWithdrawWithSystem
															.setTrantype(ECashWithdrawType.BUYCARD.name());
													lvCashWithdrawWithSystem.sendToProxyServer().setHandler(h5 -> {
														if (h5.result() != null) {
															if (h5.result().getString(FieldName.CODE).equals("P0000")) {
																this.CompleteGenerateResponse(CodeMapping.CR00.name(),
																		CodeMapping.CR00.value(), "", response);
																return;
															}
														}
														CardDao lvCardDao2 = new CardDao();
														lvCardDao2.revertThisCard(cardID);
														this.CompleteGenerateResponse(CodeMapping.P2222.name(),
																CodeMapping.P2222.value(), h5.result().getJsonObject(FieldName.DATA), response);
													});
												} else {
													CardDao lvCardDao2 = new CardDao();
													lvCardDao2.revertThisCard(cardID);
													this.CompleteGenerateResponse(CodeMapping.C4444.name(),
															CodeMapping.C4444.value(), null, response);
												}
											});
								} else {
									this.CompleteGenerateResponse(CodeMapping.CR11.name(), CodeMapping.CR11.value(),
											null, response);
								}
							});
							return;
						}
					}
					this.CompleteGenerateResponse(CodeMapping.C4444.name(), CodeMapping.C4444.value(), null, response);

				});
			} else {
				this.CompleteGenerateResponse(CodeMapping.C4444.name(), CodeMapping.C4444.value(), null, response);
			}
		});
	}

	private void newCard() {
		String categoryID = getMessageBody().getString(FieldName.CATEGORYID);
		String value = getMessageBody().getString(FieldName.VALUE);
		String amount = getMessageBody().getString(FieldName.AMOUNT);
		String seriesID = getMessageBody().getString(FieldName.SERIESID);
		String code = getMessageBody().getString(FieldName.CODE);
		CardDao card = new CardDao();
		card.newCardData(categoryID, value, amount, seriesID, code);
		card.getMvFutureResponse().setHandler(handler -> {
			response.complete(handler.result());
		});
	}

	private void retriveALl() {
		String state = getMessageBody().getString(FieldName.STATE);
		state = state==null?"":state;
		JsonObject query = new JsonObject();
		if(!state.equals("")) {
			query.put(FieldName.STATE, state);
		}
		CardDao  lvCardDao= new CardDao();
		lvCardDao.retrieveCardDetail(query).setHandler(handler->{
			this.CompleteGenerateResponse(CodeMapping.C0000.name(), "", handler.result(), response);
		});
		
	}
	
	private void disableCard() {
		String id = getMessageBody().getString(FieldName.CARDID);
		String username = getMessageBody().getString(FieldName.USERNAME);
		CardDao cardDao = new CardDao();
		cardDao.doneThisCard(id, username);
		this.CompleteGenerateResponse(CodeMapping.C0000.name(), "", null, response);
	}
}
