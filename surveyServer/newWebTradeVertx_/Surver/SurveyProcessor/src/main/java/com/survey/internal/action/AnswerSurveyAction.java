package com.survey.internal.action;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import com.survey.ProcessorInit;
import com.survey.dbservice.dao.CashDepositDao;
import com.survey.dbservice.dao.SurveySubmitDao;
import com.survey.dbservice.dao.UserDao;
import com.survey.etheaction.ProxySurveyAnswer;
import com.survey.utils.CodeMapping;
import com.survey.utils.FieldName;
import com.survey.utils.Log;
import com.survey.utils.MessageDefault;
import com.survey.utils.RSAEncrypt;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

public class AnswerSurveyAction extends InternalSurveyBaseAction {

	@Override
	public void doProccess() {
		String surveyID = getMessageBody().getString(FieldName.SURVEYID);
		String username = getMessageBody().getString(FieldName.USERNAME);
		JsonObject answerdata = getMessageBody().getJsonObject(FieldName.DATA);
		String token = getMessageBody().getString(FieldName.TOKEN);
		try {
			String descryptToken = RSAEncrypt.getIntance().decrypt(token);
			long timeout = new Date().getTime() - Long.parseLong(descryptToken.split("\\*")[1]);
			if (timeout > ProcessorInit.mvConfig.getDouble("SurveyTokenTimeOut")) {
				this.CompleteGenerateResponse(CodeMapping.C6666.toString(), "Permission deny invalid token", null,
						response);
				return;
			}
			if (surveyID.equals(descryptToken.split("\\*")[0])) {
				SurveySubmitDao lvSurveySubmitDao = new SurveySubmitDao();
				lvSurveySubmitDao.newSurveyResult(username, answerdata, surveyID).setHandler(id -> {
					if (id.succeeded()) {
						this.CompleteGenerateResponse(CodeMapping.C0000.toString(), CodeMapping.C0000.value(), null,
								response);
						Future<JsonObject> lv = Future.future();
						String answerID = id.result();

						/*
						 * VertxServiceCenter.getInstance().sendNewMessage(
						 * EventBusDiscoveryConst.ETHEREUMPROXYDISCOVERY.name(), getMessageBody(), lv);
						 * lv.setHandler(handler -> { if (handler.succeeded()) { ProxyLogDao lvDao = new
						 * ProxyLogDao();
						 * lvDao.storeNewRequest(getMessageBody().getString(FieldName.ACTION),
						 * getMessageBody(), handler.result()); } else {
						 * Log.print("[Submit Answer surveyData] Send Proxy Failed. Cause:" +
						 * handler.cause().getMessage(), Log.ERROR_LOG); } });
						 */
						// Create cash deposit (deposit from survey)
						UserDao lvUserDao = new UserDao();
						lvUserDao.doGetUserInfobyUserName(username);
						lvUserDao.getMvFutureResponse().setHandler(h -> {
							if (h.succeeded() && h.result() != null) {
								if (h.result().getString(FieldName.CODE).equals(CodeMapping.U0000.name())) {
									CashDepositDao lvCashDepositDao = new CashDepositDao();
									lvCashDepositDao.createSuveyAnswerRefund(surveyID, username, answerID).setHandler(h2 -> {
										if (h2.succeeded() && h2.result() != null) {
											String transid = h2.result();
											//Proxy to Ethe Server
											ProxySurveyAnswer lvProxySurveyAnswer = new ProxySurveyAnswer(surveyID,
													username, transid, answerdata);
											lvProxySurveyAnswer.sendToProxyServer().setHandler(h3 -> {
												if (h3.succeeded()) {
													JsonObject etheResponse = h3.result();
													if (!etheResponse.getString(FieldName.CODE).equals("P0000")) {
														CashDepositDao lvDao = new CashDepositDao();
														lvDao.updateDeposit(transid, "C", "U", etheResponse.getString(FieldName.CODE));
													}
												} else {
													Log.print("Proxy to ethe error: " + h2.cause().getMessage());
												}
											});
										} else {
											Log.print("Can not create answer payment for user: " + username
													+ " surveyid: " + surveyID);
										}
									});
								}
							}
						});

					} else {
						response.complete(MessageDefault.RequestFailed(id.cause().getMessage()));
					}
				});
			} else {
				this.CompleteGenerateResponse(CodeMapping.C6666.toString(), "Permission deny invalid token", null,
						response);

			}
		} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException
				| BadPaddingException | UnsupportedEncodingException e) {
			this.CompleteGenerateResponse(CodeMapping.C6666.toString(), "Permission deny invalid token", null,
					response);

		}
	}

}
