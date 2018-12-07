package com.survey.internal.action;

import java.util.Date;

import com.survey.dbservice.dao.CashTransactionDao;
import com.survey.dbservice.dao.UserDao;
import com.survey.etheaction.ProxyAccountBalance;
import com.survey.etheaction.ProxyTransactionTransfer;
import com.survey.utils.CodeMapping;
import com.survey.utils.FieldName;

import io.vertx.core.json.JsonObject;

public class CashTranserAction extends InternalSurveyBaseAction {

	@Override
	public void doProccess() {
		String username = getMessageBody().getString(FieldName.USERNAME);
		String toUsername = getMessageBody().getString(FieldName.TOUSER);
		double amount = Double.parseDouble(getMessageBody().getString(FieldName.AMOUNT));
		String remark = getMessageBody().getString(FieldName.REMARK);

		ProxyAccountBalance lvAccountBalance = new ProxyAccountBalance(username);
		lvAccountBalance.sendToProxyServer().setHandler(handler -> {
			if (handler.succeeded()) {
				if (handler.result().getString(FieldName.CODE).equals("P0000")) {
					double accountBalance = Double.parseDouble(
							handler.result().getJsonObject(FieldName.DATA).getValue(FieldName.BALANCE).toString());
					if (amount > accountBalance) {
						this.CompleteGenerateResponse(CodeMapping.T1111.name(), CodeMapping.T1111.value(),
								handler.result(), response);
					} else {
						UserDao lvUserDao = new UserDao();
						lvUserDao.checkUserState(toUsername).setHandler(userState -> {
							if (userState.succeeded()) {
								if (userState.result().equals("A") || userState.result().equals("N")) {
									CashTransactionDao lvCashTransactionDao = new CashTransactionDao();
									lvCashTransactionDao.createCashTransferOut(username, toUsername, amount, remark)
											.setHandler(trans -> {
												if (trans.result() != null) {
													ProxyTransactionTransfer lvTransfer = new ProxyTransactionTransfer(
															username, toUsername, String.valueOf(amount),
															trans.result());
													lvTransfer.sendToProxyServer().setHandler(h -> {
														if (h.result() != null) {
															if (h.result().getString(FieldName.CODE).equals("P0000")) {
																this.CompleteGenerateResponse(CodeMapping.T0000.name(),
																		"Success", new JsonObject()
																				.put(FieldName.TRANID, trans.result()),
																		response);
																return;
															}
														}
														// Fail
														
														this.CompleteGenerateResponse(CodeMapping.P1111.name(),
																CodeMapping.P1111.value(),
																h.result().getJsonObject(FieldName.DATA), response);
														CashTransactionDao lvCashTransactionDao2 = new CashTransactionDao();
														lvCashTransactionDao2.updateCashTransferStatus(trans.result(),
																"U", new Date().getTime(), 0, 0);

													});
												} else {
													this.CompleteGenerateResponse(CodeMapping.T1111.name(),
															CodeMapping.T1111.value(), null, response);
												}
											});
								} else {
									this.CompleteGenerateResponse(CodeMapping.T2222.name(), CodeMapping.T2222.value(),
											null, response);
								}
							} else {
								this.CompleteGenerateResponse(CodeMapping.T3333.name(), CodeMapping.T3333.value(), null,
										response);
							}
						});

					}
				} else {
					this.CompleteGenerateResponse(CodeMapping.P2222.name(), CodeMapping.P2222.value(), handler.result(),
							response);
				}
			} else {
				this.CompleteGenerateResponse(CodeMapping.P1111.name(), CodeMapping.P1111.value(), null, response);
			}
		});

	}

}
