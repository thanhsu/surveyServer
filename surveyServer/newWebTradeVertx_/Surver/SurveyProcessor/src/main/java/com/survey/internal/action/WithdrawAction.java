package com.survey.internal.action;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import com.survey.constant.EventBusDiscoveryConst;
import com.survey.dbservice.dao.CashWithdrawDao;
import com.survey.dbservice.dao.UtilsDao;
import com.survey.etheaction.ProxyAccountBalance;
import com.survey.utils.CodeMapping;
import com.survey.utils.FieldName;
import com.survey.utils.MessageDefault;
import com.survey.utils.RSAEncrypt;
import com.survey.utils.Utils;
import com.survey.utils.VertxServiceCenter;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

public class WithdrawAction extends InternalSurveyBaseAction {

	@Override
	public void doProccess() {
		String userid = getMessageBody().getString(FieldName.USERID);
		String username = getMessageBody().getString(FieldName.USERNAME);
		int amount = getMessageBody().getInteger(FieldName.AMOUNT);
		// Check balance
		Future<JsonObject> lvGetAccountBalance = Future.future();
		ProxyAccountBalance lvProxyAccountBalance = new ProxyAccountBalance(username);
		lvProxyAccountBalance.sendToProxyServer(lvGetAccountBalance);

		lvGetAccountBalance.setHandler(balance -> {
			if (balance.succeeded()) {
				double etheAmount = Double
						.parseDouble(balance.result().getJsonObject(FieldName.DATA).getValue("balance").toString());
				if (Utils.checkWithdraw(amount, etheAmount)) {
					CashWithdrawDao lvCashWithdrawDao = new CashWithdrawDao();
					UtilsDao lvUtilsDao = new UtilsDao();
					lvUtilsDao.retrieveEthePointValue(false).setHandler(point -> {
						try {
							String exchangeRate = point.result().getString(FieldName.VALUE);
							String privateToken = RSAEncrypt.getIntance().encrypt(userid);

							lvCashWithdrawDao
									.storeNewWithdrawRequest(userid, privateToken, "",
											getMessageBody().getInteger(FieldName.AMOUNT), "",
											getMessageBody().getString(FieldName.REMARK), false, exchangeRate)
									.setHandler(handler -> {
										if (handler.succeeded()) {
											String id = handler.result();
											this.response.complete(
													new JsonObject().put(FieldName.CODE, CodeMapping.C0000.toString())
															.put(FieldName.DATA,
																	new JsonObject().put("success", true)
																			.put(FieldName.PRIVATETOKEN, privateToken)
																			.put(FieldName.TRANID, id)));
										} else {
											this.response.complete(
													MessageDefault.RequestFailed(handler.cause().getMessage()));
										}
									});
						} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException
								| IllegalBlockSizeException | BadPaddingException | UnsupportedEncodingException e) {
							this.response.complete(MessageDefault.ParamError("UserID is NULL!"));
							e.printStackTrace();
						}
					});
				} else {
					this.CompleteGenerateResponse(CodeMapping.W1111.toString(), CodeMapping.W1111.value(),
							balance.result(), response);
				}
			} else {
				// debug

				double etheAmount = 100000000;
				if (Utils.checkWithdraw(amount, etheAmount)) {
					CashWithdrawDao lvCashWithdrawDao = new CashWithdrawDao();
					UtilsDao lvUtilsDao = new UtilsDao();
					lvUtilsDao.retrieveEthePointValue(false).setHandler(point -> {
						try {
							String exchangeRate = point.result().getJsonArray(FieldName.DATA).getJsonObject(0).getString(FieldName.VALUE);
							String privateToken = RSAEncrypt.getIntance().encrypt(userid);
							double rate = Double.parseDouble(exchangeRate);
							lvCashWithdrawDao
									.storeNewWithdrawRequest(userid, privateToken, "",
											getMessageBody().getInteger(FieldName.AMOUNT),
											getMessageBody().getString(FieldName.CCY),
											getMessageBody().getString(FieldName.REMARK), false, exchangeRate)
									.setHandler(handler -> {
										if (handler.succeeded()) {
											String id = handler.result();
											sendCashWithdraw(username,
													String.valueOf(
															rate * getMessageBody().getInteger(FieldName.AMOUNT)),
													getMessageBody().getString(FieldName.REMARK), "CashWithdraw", id);
											this.response.complete(
													new JsonObject().put(FieldName.CODE, CodeMapping.C0000.toString())
															.put(FieldName.DATA,
																	new JsonObject().put("success", true)
																			.put(FieldName.PRIVATETOKEN, privateToken)
																			.put(FieldName.TRANID, id)));
										} else {
											this.response.complete(
													MessageDefault.RequestFailed(handler.cause().getMessage()));
										}
									});
						} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException
								| IllegalBlockSizeException | BadPaddingException | UnsupportedEncodingException e) {
							this.response.complete(MessageDefault.ParamError("UserID is NULL!"));
							e.printStackTrace();
						}
					});
				} else {
					this.CompleteGenerateResponse(CodeMapping.W1111.toString(), CodeMapping.W1111.value(),
							balance.result(), response);
				}

				// this.response.complete(MessageDefault.RequestFailed("service is
				// unavailable"));
			}
		});
	}

	private void sendCashWithdraw(String username, String amount, String remark, String action, String tranID) {

	}
}
