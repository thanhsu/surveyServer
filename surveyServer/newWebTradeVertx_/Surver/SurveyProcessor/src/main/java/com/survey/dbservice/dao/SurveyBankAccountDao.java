package com.survey.dbservice.dao;

import java.util.Date;
import java.util.List;

import com.survey.utils.FieldName;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

public class SurveyBankAccountDao extends SurveyBaseDao {
	public static final String collectionName = "surveybankaccount";

	public SurveyBankAccountDao() {
		this.setCollectionName(collectionName);
	}

	public void retrieveAllBank(Handler<AsyncResult<List<JsonObject>>> handler) {
		this.queryDocument(new JsonObject().put(FieldName.STATE, "A"), handler);
	}

	public void addNewBank(String bankID, String bankName, String ccy, String bankAccount, String bankAccountAddress,
			String bankAccountName) {
		JsonObject data = new JsonObject();
		data.put(FieldName.BANKID, bankID);
		data.put(FieldName.BANKNAME,bankName).put(FieldName.CCY, ccy).put(FieldName.BANKACCOUNTCODE,bankAccount ).put(FieldName.BANKACCOUNTADDRESS, bankAccountAddress);
		data.put(FieldName.BANKACCOUNTNAME, bankAccountName);
		data.put(FieldName.STATE, "A").put(FieldName.INPUTTIME, new Date().getTime());
		this.saveDocument(data);
	}
}
