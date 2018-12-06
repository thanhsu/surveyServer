package com.survey.dbservice.dao;

import java.util.Date;
import com.survey.utils.CodeMapping;
import com.survey.utils.ECashDepositType;
import com.survey.utils.FieldName;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.UpdateOptions;

public class CashDepositDao extends SurveyBaseDao {
	private static final String DepositCollectionName = "deposit";

	public CashDepositDao() {
		setCollectionName(DepositCollectionName);
	}

	public Future<String> storeNewDepositRequest(String targetUsername, String privateToken, String method,
			double amount, String ccy, String remark, boolean isApproval, String exchagerate) {
		Date lvNow = new Date();
		JsonObject deposit = new JsonObject();
		deposit.put(FieldName.USERNAME, targetUsername).put(FieldName.TOKEN, privateToken).put(FieldName.AMOUNT, amount)
				.put(FieldName.STATE, "A").put(FieldName.SETTLESTATUS, "U").put(FieldName.INPUTTIME, lvNow.getTime())
				.put(FieldName.EXCHANGERATE, exchagerate).put(FieldName.CCY, ccy).put(FieldName.TYPE, ECashDepositType.CLIENTCASH);
		return this.saveDocumentReturnID(deposit);
	}

	public Future<JsonObject> retrieveAllDeposit(long fromTime, long toTime, String username) {
		JsonObject query = new JsonObject().put(FieldName.USERNAME, username).put(FieldName.INPUTTIME,
				new JsonObject().put("$lt", toTime).put("$gt", fromTime));
		this.queryDocument(query, handler -> {
			this.CompleteGenerateResponse(CodeMapping.C0000.toString(), "", handler.result());
		});
		return mvFutureResponse;
	}

	public void updateSettlesStatus(String id, String settleStatus, String cause) {
		this.updateDocument(new JsonObject().put(FieldName._ID, id), new JsonObject().put(FieldName.SETTLESTATUS, settleStatus).put(FieldName.REJECTCAUSE, cause), new UpdateOptions(false), handler->{});
	}

	public void cancelDeposit(String depositID, String userID) {
		this.queryDocument(new JsonObject().put(FieldName._ID, depositID), handler -> {
			if (handler.succeeded() && handler.result().size() > 0) {
				if (handler.result().get(0).getString(FieldName.USERID).equals(userID)) {
					this.updateDocument(new JsonObject().put(FieldName._ID, depositID),
							new JsonObject().put(FieldName.STATE, "D"), new UpdateOptions().setUpsert(false),
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

	public Future<String> createSurveyWithdraw(String surveyID, String username) {
		return this.saveDocumentReturnID(new JsonObject().put(FieldName.USERNAME, username)
				.put(FieldName.AGENT, surveyID).put(FieldName.AGENTTYPE, "survey").put(FieldName.AMOUNT, 0).put(FieldName.STATE, "A")
				.put(FieldName.SETTLESTATUS, "P").put(FieldName.TYPE, ECashDepositType.SURVEYWITHDRAW)
				.put(FieldName.INPUTTIME, new Date().getTime()));
	}
	
	public Future<String> createSurveyWithdraw(String surveyID, String username,ECashDepositType type) {
		return this.saveDocumentReturnID(new JsonObject().put(FieldName.USERNAME, username)
				.put(FieldName.AGENT, surveyID).put(FieldName.AGENTTYPE, "survey").put(FieldName.AMOUNT, 0).put(FieldName.STATE, "A")
				.put(FieldName.SETTLESTATUS, "P").put(FieldName.TYPE, type)
				.put(FieldName.INPUTTIME, new Date().getTime()));
	}

	public Future<JsonObject> updateSurveyWithdrawSettleStatus(String id, String status) {
		Future<JsonObject> lvFuture = Future.future();
		this.queryDocument(new JsonObject().put(FieldName._ID, id), handler -> {
			if (handler.succeeded() && handler.result() != null) {
				if (handler.result().isEmpty()) {
					lvFuture.fail("Null");
				} else {
					this.updateDocument(new JsonObject().put(FieldName._ID, id), new JsonObject()
							.put(FieldName.SETTLESTATUS, status).put(FieldName.UPDATETIME, new Date().getTime()),
							new UpdateOptions(false), h2 -> {
								if (h2.succeeded()) {
									this.findOneByID(id).setHandler(h3 -> {
										lvFuture.complete(h3.result());
									});
								} else {
									lvFuture.fail(h2.cause().getMessage());
								}
							});
				}
			} else {
				lvFuture.fail("Null");
			}
		});
		return lvFuture;
	}

	public Future<String> createSuveyAnswerRefund(String surveyID, String username, String answerID) {
		Future<String> lvResult = Future.future();
		JsonObject js = new JsonObject().put(FieldName.USERNAME, username).put(FieldName.AGENT, surveyID).put(FieldName.AGENTTYPE, "survey")
				.put(FieldName.AMOUNT, 0).put(FieldName.STATE, "A").put(FieldName.SETTLESTATUS, "P")
				.put(FieldName.TYPE, ECashDepositType.SURVEYANSWER);
		js.put(FieldName.INPUTTIME, new Date().getTime()).put(FieldName.ANSWERID, answerID);
		this.saveDocumentReturnID(js, lvResult);
		return lvResult;
	}
	

	public Future<JsonObject> updateDeposit(String id, String state, String settleStatus, String confirmCode) {
		Future<JsonObject> lvFuture = Future.future();
		this.queryDocument(new JsonObject().put(FieldName._ID, id), handler -> {
			if (handler.succeeded() && handler.result() != null) {
				if (handler.result().isEmpty()) {
					lvFuture.fail("Null");
				} else {
					this.updateDocument(new JsonObject().put(FieldName._ID, id), new JsonObject()
							.put(FieldName.SETTLESTATUS, settleStatus).put(FieldName.STATE, state).put(FieldName.CONFIRMCODE, confirmCode).put(FieldName.UPDATETIME, new Date().getTime()),
							new UpdateOptions(false), h2 -> {
								if (h2.succeeded()) {
									this.findOneByID(id).setHandler(h3 -> {
										lvFuture.complete(h3.result());
									});
								} else {
									lvFuture.fail(h2.cause().getMessage());
								}
							});
				}
			} else {
				lvFuture.fail("Null");
			}
		});
		return lvFuture;
	}
	

}
