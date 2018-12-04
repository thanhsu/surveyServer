package com.survey.dbservice.dao;

import com.survey.internal.action.InternalSurveyBaseAction;
import com.survey.utils.CodeMapping;
import com.survey.utils.FieldName;

import io.vertx.core.json.JsonObject;

public class CreateNewBank extends InternalSurveyBaseAction {

	@Override
	public void doProccess() {
		String bankID = getMessageBody().getString(FieldName.BANKID);
		String bankName = getMessageBody().getString(FieldName.BANKNAME);
		String bankAccCode = getMessageBody().getString(FieldName.BANKACCOUNTCODE);
		String bankAccName = getMessageBody().getString(FieldName.BANKACCOUNTNAME);
		String bankAccAddress = getMessageBody().getString(FieldName.BANKACCOUNTADDRESS);
		String ccy = getMessageBody().getString(FieldName.CCY);
		SurveyBankAccountDao lvSurveyBankAccountDao = new SurveyBankAccountDao();
		lvSurveyBankAccountDao.addNewBank(bankID, bankName, ccy, bankAccCode, bankAccAddress, bankAccName);
		this.CompleteGenerateResponse(CodeMapping.C0000.name(), "", new JsonObject(), response);
	}

}
