package com.survey.internal.action;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import com.survey.dbservice.dao.CashDepositDao;
import com.survey.dbservice.dao.CashTransactionDao;
import com.survey.dbservice.dao.CashWithdrawDao;
import com.survey.etheaction.ProxyHoldMoney;
import com.survey.utils.CodeMapping;
import com.survey.utils.FieldName;
import com.survey.utils.Log;
import com.survey.utils.MessageDefault;
import com.survey.utils.RSAEncrypt;
import com.survey.utils.Utils;
import com.survey.utils.VertxServiceCenter;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

public class PaymentAction extends InternalSurveyBaseAction {
	@Override
	public void doProccess() {
		String dw = getMessageBody().getString(FieldName.DW);
		String userID = getMessageBody().getString(FieldName.USERID);
		String username = getMessageBody().getString(FieldName.USERNAME);
		String privateToke = getMessageBody().getString(FieldName.PRIVATETOKEN);
		String discoveryKey = getMessageBody().getString(FieldName.DISCOVERYKEY);
		String trandID = getMessageBody().getString(FieldName.TRANID);
		JsonObject message = getMessageBody();
		switch (dw) {
		case "deposit":
			if (checkToken(userID, privateToke)) {
				CashDepositDao lvCashDepositDao = new CashDepositDao();
				lvCashDepositDao.queryDocument(new JsonObject().put(FieldName._ID, trandID), handler -> {
					if (handler.succeeded() && handler.result() != null) {
						if (handler.result().size() > 0) {
							message.mergeIn(handler.result().get(0));
							Future<JsonObject> lvFuture = Future.future();
							VertxServiceCenter.getInstance().sendNewMessage(discoveryKey, message, lvFuture);
							lvFuture.setHandler(res -> {
								if (res.succeeded()) {
									JsonObject lvRes = res.result();
									// Update to Awaitting settle
									// Store cash tracsaction
									/*
									 * CashTransactionDao lvCashTransactionDao = new CashTransactionDao();
									 * lvCashTransactionDao.newTransaction(username, trandID, "D",
									 * lvRes.getJsonObject("data"), "P");
									 */
									this.CompleteGenerateResponse(CodeMapping.C0000.name(), "OK", lvRes, response);
								} else {
									this.response.complete(MessageDefault.RequestFailed(res.cause().getMessage()));
								}
							});
						} else {
							this.response.complete(
									MessageDefault.RequestFailed(CodeMapping.D1111.name(), CodeMapping.D1111.value()));
						}
					} else {
						this.response.complete(
								MessageDefault.RequestFailed(CodeMapping.D1111.name(), CodeMapping.D1111.value()));
					}
				});
			} else {
				this.response.complete(MessageDefault.PermissionError());
			}
			break;
		case "withdraw":
			if (checkToken(userID, privateToke)) {
				CashWithdrawDao lvCashWithdrawDao = new CashWithdrawDao();
				lvCashWithdrawDao.queryDocument(new JsonObject().put(FieldName._ID, trandID), handler -> {
					if (handler.succeeded() && handler.result() != null) {
						if (handler.result().get(0).getString(FieldName.USERID).equals(userID)) {
							message.mergeIn(handler.result().get(0));
							double amount = message.getDouble(FieldName.POINT);
							float rate = message.getFloat(FieldName.EXCHANGERATE);
							// check ccy va ti gia de chuyen qua USD
							message.put(FieldName.AMOUNT, amount * rate);
							Utils.autoApprovelCashWithdraw(message.getString(FieldName.METHOD), amount * rate)
									.setHandler(check -> {
										if (check.succeeded()) {
											// Send Payment
											
											CashWithdrawDao lvCashWithdrawDao2 = new CashWithdrawDao();
											lvCashWithdrawDao2.updateWithdrawPayment(discoveryKey, trandID, message)
													.setHandler(ca -> {
														ProxyHoldMoney lvHoldMoney = new ProxyHoldMoney();
														lvHoldMoney.setUsername(username);
														lvHoldMoney.setTransid(trandID);
														lvHoldMoney.setAmount(String.valueOf(amount));
														lvHoldMoney.sendToProxyServer().setHandler(prox -> {
															if (prox.result() != null) {
																if (prox.result().getString(FieldName.CODE)
																		.equals("P0000")) {
																	Log.print("Hold success, please wait confirm");
																	this.CompleteGenerateResponse(CodeMapping.C0000.name(), "Success please wait confirm", null, response);
																	return;
																}
															}
															this.CompleteGenerateResponse(CodeMapping.W1111.name(),
																	"Can not hold money!", prox.result(), response);
														});
													});

										/*	VertxServiceCenter.getInstance().sendNewMessage(discoveryKey, message,
													lvFuture);
											lvFuture.setHandler(sendToPaypal -> {
												if (sendToPaypal.succeeded()) {
													if (sendToPaypal.result().getBoolean("success") && sendToPaypal
															.result().getJsonObject(FieldName.DATA) != null) {
														// Send to paypal success
														JsonObject data = sendToPaypal.result()
																.getJsonObject(FieldName.DATA);
														JsonObject responseDt = data.getJsonObject("response");
														if (responseDt.getJsonObject("batch_header")
																.getString("batch_status").equals("PENDING")) {
															String link = responseDt.getJsonArray("links")
																	.getJsonObject(0).getString("href");
															lvCashWithdrawDao.updateSettlesStatusPend(
																	message.getString(FieldName._ID), "P",
																	message.getString(FieldName.IPADDRESS),
																	message.getString(FieldName.MACADDRESS), link);
															CashWithdrawDao lvCashWithdrawDao1 = new CashWithdrawDao();
															lvCashWithdrawDao1.queryDocument(
																	new JsonObject().put(FieldName._ID, trandID),
																	newwithdraw -> {
																		response.complete(newwithdraw.result().get(0));
																	});
														} else {
															lvCashWithdrawDao.updateSettlesStatus(
																	message.getString(FieldName._ID), "U",
																	message.getString(FieldName.IPADDRESS),
																	message.getString(FieldName.MACADDRESS), responseDt
																			.getJsonObject("batch_header").toString());
														}

													} else {
														// Send to paypal fail
														// store cash transaction with state = P
														lvCashWithdrawDao.updateSettlesStatus(
																message.getString(FieldName._ID), "U",
																message.getString(FieldName.IPADDRESS),
																message.getString(FieldName.MACADDRESS),
																sendToPaypal.result().getString("cause"));

														
														 * CashTransactionDao lvCashTransactionDao = new
														 * CashTransactionDao();
														 * lvCashTransactionDao.newTransaction(username, trandID, "D",
														 * new JsonObject(), "U");
														 
														CashWithdrawDao lvCashWithdrawDao1 = new CashWithdrawDao();
														lvCashWithdrawDao1.queryDocument(
																new JsonObject().put(FieldName._ID, trandID),
																newwithdraw -> {
																	response.complete(newwithdraw.result().get(0));
																});
													}
												} else {
													// Send to paypal success
													// store cash transaction with state = U
													lvCashWithdrawDao.updateSettlesStatus(
															message.getString(FieldName._ID), "U",
															message.getString(FieldName.IPADDRESS),
															message.getString(FieldName.MACADDRESS),
															sendToPaypal.cause().getMessage());
													CashTransactionDao lvCashTransactionDao = new CashTransactionDao();
													lvCashTransactionDao.newTransaction(username, trandID, "D",
															new JsonObject(), "U");
													CashWithdrawDao lvCashWithdrawDao1 = new CashWithdrawDao();
													lvCashWithdrawDao1.queryDocument(
															new JsonObject().put(FieldName._ID, trandID),
															newwithdraw -> {
																response.complete(newwithdraw.result().get(0));
															});
												}
											});*/
										} else {
											// store cash transaction with state = P
											lvCashWithdrawDao.updateSettlesStatus(message.getString(FieldName._ID), "P",
													message.getString(FieldName.IPADDRESS),
													message.getString(FieldName.MACADDRESS), "Peding Approval");

											CashWithdrawDao lvCashWithdrawDao1 = new CashWithdrawDao();
											lvCashWithdrawDao1.queryDocument(
													new JsonObject().put(FieldName._ID, trandID), newwithdraw -> {
														response.complete(newwithdraw.result().get(0));
													});
										}
									});
						} else {
							response.complete(MessageDefault.PermissionError());
						}
					} else {
						this.response.complete(
								MessageDefault.RequestFailed(CodeMapping.W1111.name(), CodeMapping.W1111.value()));
					}
				});
			} else {
				this.response.complete(MessageDefault.PermissionError());
			}
			break;
		default:
			this.response.complete(MessageDefault.ActionNotFound());
			break;
		}
	}

	private boolean checkToken(String userID, String privateToken) {
		String decode;
		try {
			decode = RSAEncrypt.getIntance().decrypt(privateToken);
			return decode.equals(userID);
		} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException
				| BadPaddingException | UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	private String generateTransferDetail(String username, String tranID, String amount) {
		return "Nộp tiền vào tài khoản " + username + " mã giao dịch " + tranID + " số tiền " + amount;
	}

}
