package com.survey.dbservice.dao;

import java.util.Date;
import java.util.List;

import com.survey.utils.FieldName;

import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.UpdateOptions;

public class CashTransactionDao extends SurveyBaseDao {
	public static final String cashtransactioncollection = "cashtransaction";

	public CashTransactionDao() {
		setCollectionName(cashtransactioncollection);
	}

	public void newTransaction(String username, String tranID, String dw, JsonObject tranData, String status) {
		this.saveDocument(new JsonObject().put(FieldName.USERNAME, username).put(FieldName.TRANID, tranID)
				.put(FieldName.DW, dw).put(FieldName.TRANSACTIONDATA, tranData));
	}

	public Future<String> createCashTransferOut(String username, String toUsername, double amount, String remark) {
		Future<String> lvResult = Future.future();
		JsonObject js = new JsonObject();
		js.put(FieldName.USERNAME, username).put(FieldName.TOUSER, toUsername).put(FieldName.INITAMOUNT, amount)
				.put(FieldName.REMARK, remark);
		js.put(FieldName.STATE, "A").put(FieldName.SETTLESTATUS, "P").put(FieldName.INPUTTIME, new Date().getTime())
				.put(FieldName.TYPE, "TRANSFER");
		this.saveDocumentReturnID(js, lvResult);
		return lvResult;
	}

	public Future<JsonObject> updateCashTransferStatus(String transID, String settleStatus, long settleTime,
			double amount, double fee) {
		Future<JsonObject> lvFuture = Future.future();
		this.updateDocument(
				new JsonObject().put(FieldName._ID, transID), new JsonObject().put(FieldName.SETTLESTATUS, settleStatus)
						.put(FieldName.SETTLESTIME, settleTime).put(FieldName.AMOUNT, amount).put(FieldName.FEE, fee),
				new UpdateOptions(false), handler -> {
					if (handler.succeeded()) {
						lvFuture.complete(new JsonObject());
					} else {
						lvFuture.fail(handler.cause().getMessage());
					}
				});
		return lvFuture;
	}

	

	public void retrieveListCashTransferIn(String username, long fromTime, long toTime,
			Future<JsonArray> resultHandler) {
		this.queryDocumentRunCmd(
				new JsonObject().put(FieldName.TOUSER, username).put(FieldName.SETTLESTIME,
						new JsonObject().put("$lt", toTime).put("$gt", fromTime)),
				new JsonObject(), new JsonObject().put(FieldName.SETTLESTIME, 1), resultHandler);

	}

	public void retrieveListCashTransferOut(String username, long fromTime, long toTime,
			Future<JsonArray> resultHandler) {
		this.queryDocumentRunCmd(
				new JsonObject().put(FieldName.USERNAME, username).put(FieldName.INPUTTIME,
						new JsonObject().put("$lt", toTime).put("$gt", fromTime)),
				new JsonObject(), new JsonObject().put(FieldName.INPUTTIME, 1), resultHandler);
	}
}
