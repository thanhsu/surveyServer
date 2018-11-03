package com.survey.dbservice.dao;

import com.survey.utils.FieldName;

import io.vertx.core.json.JsonObject;

public class CashTransactionDao extends SurveyBaseDao {
	public static final String cashtransactioncollection = "cashtransaction";

	public CashTransactionDao() {
		setCollectionName(cashtransactioncollection);
	}

	public void newTransaction(String username, String tranID,String dw, JsonObject tranData, String status) {
		this.saveDocument(new JsonObject().put(FieldName.USERNAME, username).put(FieldName.TRANID, tranID).put(FieldName.DW, dw)
				.put(FieldName.TRANSACTIONDATA, tranData));
	}
}
