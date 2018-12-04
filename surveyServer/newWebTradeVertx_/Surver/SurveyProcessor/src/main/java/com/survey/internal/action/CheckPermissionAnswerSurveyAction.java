package com.survey.internal.action;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import com.survey.dbservice.dao.SurveyDao;
import com.survey.etheaction.ProxySurveyBalance;
import com.survey.utils.CodeMapping;
import com.survey.utils.FieldName;
import com.survey.utils.RSAEncrypt;

import io.vertx.core.json.JsonObject;

public class CheckPermissionAnswerSurveyAction extends InternalSurveyBaseAction {
	@Override
	public void doProccess() {
		String username = getMessageBody().getString(FieldName.USERNAME);
		String surveyID = getMessageBody().getString(FieldName.SURVEYID);
		JsonObject loginData = getMessageBody().getJsonObject("logindata");
		// Check point of survey remain
		ProxySurveyBalance lvBalance = new ProxySurveyBalance(surveyID);
		lvBalance.sendToProxyServer().setHandler(handler -> {
			if (handler.succeeded()) {
				JsonObject msg = handler.result();
				if (msg.getString(FieldName.CODE).equals("P0000")) {
					double surveyBalance = Double.parseDouble(msg.getJsonObject(FieldName.DATA).getValue(FieldName.BALANCE).toString());
					//Need check with point/1_answer
					if(surveyBalance>0) {
						SurveyDao lvDao = new SurveyDao();
						lvDao.CheckPermisstionDoing(username, surveyID, loginData);
						lvDao.getMvFutureResponse().setHandler(handler2 -> {
							if (handler2.result().getString(FieldName.CODE).equals(CodeMapping.C0000.toString())) {
								try {
									handler2.result().put(FieldName.TOKEN,
											RSAEncrypt.getIntance().encrypt(surveyID + "*" + new Date().getTime()));
								} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException
										| IllegalBlockSizeException | BadPaddingException | UnsupportedEncodingException e) {
									e.printStackTrace();
								}
							}
							response.complete(handler2.result());
						});
					}else {
						this.CompleteGenerateResponse(CodeMapping.S3335.name(), CodeMapping.S3335.value(), null, response);
					}
				} else {
					this.CompleteGenerateResponse(CodeMapping.S1111.name(), CodeMapping.S1111.value(), null, response);
				}
			} else {
				this.CompleteGenerateResponse(CodeMapping.P1111.name(), CodeMapping.C1111.value(), null, response);
			}
		});

		
	}
}
