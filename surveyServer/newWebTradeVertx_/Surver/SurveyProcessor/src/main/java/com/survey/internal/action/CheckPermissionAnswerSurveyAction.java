package com.survey.internal.action;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import com.survey.dbservice.dao.SurveyDao;
import com.survey.utils.CodeMapping;
import com.survey.utils.FieldName;
import com.survey.utils.RSAEncrypt;

public class CheckPermissionAnswerSurveyAction extends InternalSurveyBaseAction {
	@Override
	public void doProccess() {
		String username = getMessageBody().getString(FieldName.USERNAME);
		String surveyID = getMessageBody().getString(FieldName.SURVEYID);
		// Check point of survey remain

		SurveyDao lvDao = new SurveyDao();
		lvDao.CheckPermisstionDoing(username, surveyID);
		lvDao.getMvFutureResponse().setHandler(handler->{
			if(handler.result().getString(FieldName.CODE).equals(CodeMapping.C0000.toString())) {
				try {					
					handler.result().put(FieldName.TOKEN, RSAEncrypt.getIntance().encrypt(surveyID));
				} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException
						| IllegalBlockSizeException | BadPaddingException | UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			response.complete(handler.result());
		});
	}
}
