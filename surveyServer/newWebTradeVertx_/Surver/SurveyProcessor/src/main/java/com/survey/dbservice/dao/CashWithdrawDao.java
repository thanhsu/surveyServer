package com.survey.dbservice.dao;

import java.util.Date;

import com.sun.tools.javac.jvm.Code;
import com.survey.utils.CodeMapping;
import com.survey.utils.ECashWithdrawType;
import com.survey.utils.FieldName;

import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.UpdateOptions;

public class CashWithdrawDao extends SurveyBaseDao {
	public static final String cashWithdrawCollectionName = "cashwithdraw";

	public CashWithdrawDao() {
		this.setCollectionName(cashWithdrawCollectionName);
	}

	public Future<String> storeNewWithdrawRequest(String targetUserID, String privateToken, String method,
			double amount, String ccy, String remark, boolean isApproval, double exchagerate) {
		Date lvNow = new Date();
		JsonObject deposit = new JsonObject();
		deposit.put(FieldName.USERID, targetUserID).put(FieldName.TOKEN, privateToken).put(FieldName.POINT, amount)
				.put(FieldName.STATE, "A").put(FieldName.SETTLESTATUS, "P").put(FieldName.INPUTTIME, lvNow.getTime())
				.put(FieldName.EXCHANGERATE, exchagerate).put(FieldName.TYPE, ECashWithdrawType.CLIENTCASH).put(FieldName.CCY, ccy);
		return this.saveDocumentReturnID(deposit);
	}

	public Future<String> storeNewSurveyPushlishRequest(String username, double amount, String ccy) {
		Date lvNow = new Date();
		JsonObject deposit = new JsonObject();
		deposit.put(FieldName.USERNAME, username).put(FieldName.POINT, amount).put(FieldName.STATE, "A")
				.put(FieldName.SETTLESTATUS, "P").put(FieldName.INPUTTIME, lvNow.getTime()).put(FieldName.CCY, ccy);
		deposit.put(FieldName.TYPE, ECashWithdrawType.SURVEYPUSHLISH.name());
		return this.saveDocumentReturnID(deposit);
	}

	public Future<Void> updateWithdrawPayment(String disKey, String id, JsonObject paymentDetail) {
		Future<Void> re = Future.future();
		this.updateDocument(new JsonObject().put(FieldName._ID, id),
				new JsonObject().put(FieldName.DISCOVERYKEY, disKey).put(FieldName.STATUS, "P").put(FieldName.PAYMENTDETAIL, paymentDetail),
				new UpdateOptions(false), handler -> {
					re.complete();
				});
		return re;
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

	public Future<JsonObject> retrieveAllWithdraw(long fromTime, long toTime, String username, String userid,
			String settleStatus) {
		JsonArray jar = new JsonArray().add(new JsonObject().put(FieldName.USERNAME, username))
				.add(new JsonObject().put(FieldName.USERID, userid));
		JsonObject query = new JsonObject().put("$or", jar).put(FieldName.INPUTTIME,
				new JsonObject().put("$lt", toTime).put("$gt", fromTime));
		if (!settleStatus.equals("")) {
			query.put(FieldName.SETTLESTATUS, settleStatus);
		}
		this.queryDocument(query, handler -> {
			this.CompleteGenerateResponse(CodeMapping.C0000.toString(), "", handler.result());
		});
		return mvFutureResponse;
	}

	public void updateSettlesStatus(String id, String settleStatus, String ip, String macaddress, String cause) {
		this.updateDocument(new JsonObject().put(FieldName._ID, id),
				new JsonObject().put(FieldName.SETTLESTATUS, settleStatus).put(FieldName.IPADDRESS, ip)
						.put(FieldName.MACADDRESS, macaddress).put(FieldName.REJECTCAUSE, cause).put(FieldName.SETTLESTIME, new Date().getTime()),
				new UpdateOptions(false), handler -> {
					this.mvFutureResponse.complete();
				});
	}

	public void updateSettlesStatusPend(String id, String settleStatus, String ip, String macaddress,
			String confirmLink) {
		this.updateDocument(new JsonObject().put(FieldName._ID, id),
				new JsonObject().put(FieldName.SETTLESTATUS, settleStatus).put(FieldName.IPADDRESS, ip)
						.put(FieldName.MACADDRESS, macaddress).put(FieldName.LINK, confirmLink),
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
						js.put(FieldName.INPUTTIME, new Date().getTime());
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
	
	public Future<JsonObject> updateCashWithdrawStatusCard(String transID, String settleStatus, long settleTime,
			double amount, double fee) {
		Future<JsonObject> lvFuture = Future.future();
		JsonObject data = new JsonObject().put(FieldName.SETTLESTATUS, settleStatus)
				.put(FieldName.SETTLESTIME, settleTime).put(FieldName.AMOUNT, amount).put(FieldName.FEE, fee);
		if (settleStatus.equals("U")) {
			this.updateDocument(new JsonObject().put(FieldName._ID, transID), data, new UpdateOptions(false),
					handler -> {
						if (handler.succeeded()) {
							lvFuture.complete(new JsonObject());
						} else {
							lvFuture.fail(handler.cause().getMessage());
						}
					});
		} else {
			this.findOneByID(transID).setHandler(handler -> {
				if (handler.result() != null) {
					String cardID = handler.result().getString(FieldName.CARDID);
					CardDao lvCardDao = new CardDao();
					lvCardDao.retrieveCardDetail(cardID).setHandler(h2 -> {
						data.put(FieldName.CARDDETAIL, h2.result());
						this.updateDocument(new JsonObject().put(FieldName._ID, transID), data,
								new UpdateOptions(false), h3 -> {
									if (h3.succeeded()) {
										lvFuture.complete(new JsonObject());
									} else {
										lvFuture.fail(h3.cause().getMessage());
									}
								});
					});
				}else {
					lvFuture.fail("null");
				}
			});

		}

		return lvFuture;
	}

}
