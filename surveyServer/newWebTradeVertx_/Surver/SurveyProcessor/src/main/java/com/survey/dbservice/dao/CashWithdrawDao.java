package com.survey.dbservice.dao;

import java.util.Date;

import com.sun.tools.javac.jvm.Code;
import com.survey.utils.CodeMapping;
import com.survey.utils.ECashWithdrawType;
import com.survey.utils.FieldName;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.UpdateOptions;

public class CashWithdrawDao extends SurveyBaseDao {
	public static final String cashWithdrawCollectionName = "cashwithdraw";

	public CashWithdrawDao() {
		this.setCollectionName(cashWithdrawCollectionName);
	}

	public Future<String> storeNewWithdrawRequest(String targetUserID, String privateToken, String method,
			double amount, String ccy, String remark, boolean isApproval, String exchagerate) {
		Date lvNow = new Date();
		JsonObject deposit = new JsonObject();
		deposit.put(FieldName.USERID, targetUserID).put(FieldName.TOKEN, privateToken).put(FieldName.POINT, amount)
				.put(FieldName.STATE, "A").put(FieldName.SETTLESTATUS, "U").put(FieldName.INPUTTIME, lvNow.getTime())
				.put(FieldName.EXCHANGERATE, exchagerate).put(FieldName.CCY, ccy);
		return this.saveDocumentReturnID(deposit);
	}

	public Future<String> storeNewWithdrawBuyCard(double amount, String ccy, String remark, double exchagerate,
			String cardID) {
		Date lvNow = new Date();
		JsonObject deposit = new JsonObject();
		deposit.put(FieldName.POINT, amount).put(FieldName.STATE, "A").put(FieldName.SETTLESTATUS, "U")
				.put(FieldName.INPUTTIME, lvNow.getTime()).put(FieldName.EXCHANGERATE, exchagerate)
				.put(FieldName.CCY, ccy);
		deposit.put(FieldName.TYPE, ECashWithdrawType.BUYCARD.name());
		deposit.put(FieldName.CARDID, cardID);
		return this.saveDocumentReturnID(deposit);
	}

	public Future<JsonObject> retrieveAllWithdraw(long fromTime, long toTime, String username) {
		JsonObject query = new JsonObject().put(FieldName.USERNAME, username).put(FieldName.INPUTTIME,
				new JsonObject().put("$lt", toTime).put("$gt", fromTime));
		this.queryDocument(query, handler -> {
			this.CompleteGenerateResponse(CodeMapping.C0000.toString(), "", handler.result());
		});
		return mvFutureResponse;
	}

	public void updateSettlesStatus(String id, String settleStatus, String ip, String macaddress) {
		this.updateDocument(
				new JsonObject().put(FieldName._ID, id), new JsonObject().put(FieldName.SETTLESTATUS, settleStatus)
						.put(FieldName.IPADDRESS, ip).put(FieldName.MACADDRESS, macaddress),
				new UpdateOptions(false), handler -> {

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

	public Future<String> createSurveyCashDeposit(String pSurveyID, String username, String pAmount, String remark) {
		Future<String> lvResult = Future.future();
		
		SurveyDao lvSurveyDao = new SurveyDao();
		lvSurveyDao.retrieveSurvey(new JsonObject().put(FieldName._ID, pSurveyID), h -> {
			if (h.succeeded() && h.result() != null) {
				if (h.result().isEmpty()) {
					lvResult.fail(CodeMapping.S1111.name());
				} else {
					String lvState = h.result().get(0).getString(FieldName.STATE);
					if (lvState.equals("D")) {
						lvResult.fail(CodeMapping.S0002.name());
					} else {
						JsonObject js = new JsonObject();
						js.put(FieldName.USERNAME, username).put(FieldName.SURVEYID, pSurveyID).put(FieldName.AMOUNT,
								pAmount);
						js.put(FieldName.TYPE, ECashWithdrawType.SURVEYDEPOSIT.name());
						js.put(FieldName.STATE, "A");
						js.put(FieldName.SETTLESTATUS, "P");
						js.put(FieldName.REMARK, remark);
						this.saveDocumentReturnID(js, lvResult);
					}
				}
			} else {
				lvResult.fail("Survey not found");
			}
		});

		return lvResult;
	}

	public Future<JsonObject> updateTransStatus(String tranID, String status, String confirmCode) {
		Future<JsonObject> lvTranData = Future.future();
		this.updateDocument(new JsonObject().put(FieldName._ID, tranID),
				new JsonObject().put(FieldName.SETTLESTATUS, status).put(FieldName.CONFIRMCODE, confirmCode),
				new UpdateOptions(false), handler -> {
					if (handler.succeeded()) {
						this.queryDocument(new JsonObject().put(FieldName._ID, tranID), h2 -> {
							if (h2.succeeded() && h2.result() != null) {
								if (h2.result().isEmpty()) {
									lvTranData.fail("Non Exists");
								} else {
									lvTranData.complete(h2.result().get(0));
								}
							} else {
								lvTranData.fail("Non Exists");
							}
						});
					} else {
						lvTranData.fail("Non Exists");
					}
				});
		return lvTranData;
	}

}
