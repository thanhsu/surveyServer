package com.survey.dbservice.dao;

import java.util.Date;

import com.survey.utils.CodeMapping;
import com.survey.utils.FieldName;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.UpdateOptions;

public class SurveyCashDWDao extends SurveyBaseDao {
	public static final String collectionName = "surveycasddw";

	public SurveyCashDWDao() {
		setCollectionName(collectionName);
	}

	public Future<String> cashDeposit(String surveyID, String username, String amount, String remark, String state,
			String settleStatus) {
		JsonObject deposit = new JsonObject();
		deposit.put(FieldName.DW, "D");
		deposit.put(FieldName.SURVEYID, surveyID);
		deposit.put(FieldName.USERNAME, username);
		deposit.put(FieldName.AMOUNT, amount);
		deposit.put(FieldName.REMARK, remark);
		deposit.put(FieldName.STATE, state);
		deposit.put(FieldName.SETTLESTATUS, settleStatus);
		deposit.put(FieldName.INPUTTIME, new Date().getTime());

		return this.saveDocumentReturnID(deposit);
	}

	public void updateSettleStatus(String id, String settle) {
		this.updateDocument(new JsonObject().put(FieldName._ID, id),
				new JsonObject().put(FieldName.SETTLESTATUS, settle), new UpdateOptions(), handler -> {
					this.CompleteGenerateResponse(CodeMapping.C0000.toString(), "done", null);
				});
	}

	public Future<String> cashWithdraw(String surveyID, String username, String amount, String remark, String state,
			String settleStatus) {
		JsonObject withdraw = new JsonObject();
		withdraw.put(FieldName.DW, "W");
		withdraw.put(FieldName.SURVEYID, surveyID);
		withdraw.put(FieldName.USERNAME, username);
		withdraw.put(FieldName.AMOUNT, amount);
		withdraw.put(FieldName.REMARK, remark);
		withdraw.put(FieldName.STATE, state);
		withdraw.put(FieldName.SETTLESTATUS, settleStatus);
		withdraw.put(FieldName.INPUTTIME, new Date().getTime());
		return this.saveDocumentReturnID(withdraw);
	}
}
