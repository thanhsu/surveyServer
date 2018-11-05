package com.survey.dbservice.dao;

import java.util.Date;

import com.survey.utils.CodeMapping;
import com.survey.utils.FieldName;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.UpdateOptions;

public class CashWithdrawDao extends SurveyBaseDao {
	public static final String cashWithdrawCollectionName = "cashwithdraw";

	public CashWithdrawDao() {
		this.setCollectionName(cashWithdrawCollectionName);
	}

	public Future<String> storeNewWithdrawRequest(String targetUserID, String privateToken, String method, int amount,
			String ccy, String remark, boolean isApproval, String exchagerate) {
		Date lvNow = new Date();
		JsonObject deposit = new JsonObject();
		deposit.put(FieldName.USERID, targetUserID).put(FieldName.TOKEN, privateToken).put(FieldName.POINT, amount)
				.put(FieldName.STATE, "A").put(FieldName.SETTLESTATUS, "U").put(FieldName.INPUTTIME, lvNow.getTime())
				.put(FieldName.EXCHANGERATE, exchagerate).put(FieldName.CCY, ccy);
		return this.saveDocumentReturnID(deposit);
	}

	public Future<JsonObject> retrieveAllWithdraw(long fromTime, long toTime, String userID) {
		JsonObject query =  new JsonObject().put(FieldName.USERID, userID).put(FieldName.INPUTTIME,
				new JsonObject().put("$lt", toTime).put("$gt", fromTime));
		this.queryDocument(query, handler -> {
			this.CompleteGenerateResponse(CodeMapping.C0000.toString(), "", handler.result());
		});
		return mvFutureResponse;
	}

	public void updateSettlesStatus(String id, String settleStatus, String ip, String macaddress) {
		this.updateDocument(new JsonObject().put(FieldName._ID, id), new JsonObject().put(FieldName.SETTLESTATUS, settleStatus).put(FieldName.IPADDRESS, ip).put(FieldName.MACADDRESS, macaddress), new UpdateOptions(false), handler->{
			
		});
	}

	public void cancelWithdraw(String depositID, String userID) {
		this.queryDocument(new JsonObject().put(FieldName._ID, depositID), handler -> {
			if (handler.succeeded() && handler.result().size() > 0) {
				if (handler.result().get(0).getString(FieldName.USERID).equals(userID)) {
					this.updateDocument(new JsonObject().put(FieldName._ID, depositID),
							new JsonObject().put(FieldName.STATE, "C"), new UpdateOptions().setUpsert(false),
							handler2 -> {
								this.CompleteGenerateResponse(CodeMapping.C0000.toString(), "Success",
										new JsonObject().put("success", handler2.succeeded()));
							});
				} else {
					this.CompleteGenerateResponse(CodeMapping.C6666.toString(), CodeMapping.C6666.value(), null);
				}
			} else {
				this.CompleteGenerateResponse(CodeMapping.D1111.toString(), CodeMapping.D1111.value(), null);
			}
		});
	}

}