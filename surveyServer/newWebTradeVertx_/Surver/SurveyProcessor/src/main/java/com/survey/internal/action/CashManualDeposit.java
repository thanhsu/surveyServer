package com.survey.internal.action;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import com.survey.dbservice.dao.CashDepositDao;
import com.survey.dbservice.dao.SurveyBankAccountDao;
import com.survey.utils.CashManualUtils;
import com.survey.utils.CodeMapping;
import com.survey.utils.FieldName;
import com.survey.utils.MessageDefault;
import com.survey.utils.RSAEncrypt;

import io.vertx.core.json.JsonObject;

public class CashManualDeposit extends InternalSurveyBaseAction {
	@Override
	public void doProccess() {
		String username = getMessageBody().getString(FieldName.USERNAME);
		String transId = getMessageBody().getString(FieldName.TRANID);
		String userID = getMessageBody().getString(FieldName.USERID);
		String privateToken = getMessageBody().getString(FieldName.PRIVATETOKEN);
		if (checkToken(userID, privateToken)) {
			CashDepositDao lvCashDepositDao = new CashDepositDao();
			lvCashDepositDao.queryDocument(new JsonObject().put(FieldName._ID, transId), handler -> {
				if (handler.succeeded() && handler.result() != null) {
					if (!handler.result().isEmpty()) {
						SurveyBankAccountDao lvSurveyBankAccountDao = new SurveyBankAccountDao();
						lvSurveyBankAccountDao.retrieveAllBank(lstBank->{
							if(lstBank.succeeded()&&lstBank.result()!=null) {
								if(!lstBank.result().isEmpty()) {
									JsonObject messageResponse = new JsonObject();
									messageResponse.put(FieldName.DEPOSITDATA, handler.result().get(0));
									messageResponse.put(FieldName.LISTBANK, lstBank.result());
									messageResponse.put(FieldName.REGEXTRANSFERREMARK, CashManualUtils.genDepositRegex(username, transId));
									this.CompleteGenerateResponse(CodeMapping.C0000.name(), "Oke", messageResponse, response);
									return;
								}
							}
							this.CompleteGenerateResponse(CodeMapping.D2222.name(), CodeMapping.D2222.value(), null, response);
						});
						return;
					}
				}
				this.response
						.complete(MessageDefault.RequestFailed(CodeMapping.D1111.name(), CodeMapping.D1111.value()));
			});
		} else {
			this.response.complete(MessageDefault.PermissionError());
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
