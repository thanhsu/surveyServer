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
import com.survey.utils.CodeMapping;
import com.survey.utils.FieldName;
import com.survey.utils.MessageDefault;
import com.survey.utils.RSAEncrypt;
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
									// Store cash tracsaction
									CashTransactionDao lvCashTransactionDao = new CashTransactionDao();
									lvCashTransactionDao.newTransaction(username, trandID, lvRes.getJsonObject("data"));
									this.response.complete(lvRes);
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
				lvCashWithdrawDao.queryDocument(new JsonObject().put(FieldName._ID, trandID) , handler->{
				if(handler.succeeded())){
					
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

}
