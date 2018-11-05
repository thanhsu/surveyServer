package com.survey.dbservice.dao;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import com.survey.constant.EventBusDiscoveryConst;
import com.survey.utils.CodeMapping;
import com.survey.utils.FieldName;
import com.survey.utils.VertxServiceCenter;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class SurveyDao extends SurveyBaseDao {
	private static final String SurveyCollectionName = "survey_datas";

	public SurveyDao() {
		setCollectionName(SurveyCollectionName);
	}

	public void retrieveSurvey(JsonObject query, Handler<AsyncResult<List<JsonObject>>> h) {
		this.queryDocument(query, h);
	}

	public Future<String> createSurvey(String username, String title, JsonArray categoryID, String description) {
		JsonObject tmpSurvey = new JsonObject();
		tmpSurvey.put(FieldName.USERNAME, username);
		tmpSurvey.put(FieldName.TITLE, title);
		tmpSurvey.put(FieldName.ISTEMP, false);
		tmpSurvey.put(FieldName.DESCRIPTION, description);
		tmpSurvey.put(FieldName.CATEGORY, categoryID);
		tmpSurvey.put(FieldName.STATE, "A");
		tmpSurvey.put(FieldName.STATUS, "L");
		tmpSurvey.put(FieldName.INPUTTIME, new Date().getTime());
		return this.saveDocumentReturnID(tmpSurvey);
	}

	public Future<String> newSurvey(String username, String title, JsonArray listcategoryid, JsonArray questionData,
			JsonObject setting, String ruleData, String themeId) {
		JsonObject tmpSurvey = new JsonObject();
		tmpSurvey.put(FieldName.USERNAME, username);
		tmpSurvey.put(FieldName.TITLE, title);
		tmpSurvey.put(FieldName.LISTCATEGORYID, listcategoryid);
		tmpSurvey.put(FieldName.QUESTIONDATA, questionData);
		tmpSurvey.put(FieldName.STATE, "A");
		tmpSurvey.put(FieldName.STATUS, "L");
		tmpSurvey.put(FieldName.SETTING, setting);
		tmpSurvey.put(FieldName.THEMEID, themeId);

		tmpSurvey.put(FieldName.INPUTTIME, new JsonObject().put("$date", new Timestamp(new Date().getTime())));
		return this.saveDocumentReturnID(tmpSurvey);
	}

	public void UpdateSurvey(String surveyID, JsonArray questionData, JsonObject settingID, JsonObject ruleData,
			JsonObject theme, String title) {
		JsonObject tmpSurvey = new JsonObject();
		if (questionData != null) {
			tmpSurvey.put(FieldName.QUESTIONDATA, questionData);
		}
		if (settingID != null) {
			tmpSurvey.put(FieldName.SETTING, settingID);
		}
		if (theme != null) {
			tmpSurvey.put(FieldName.THEME, theme);
		}
		if (ruleData != null) {
			tmpSurvey.put(FieldName.RULE, ruleData);
		}
		if (title != null) {
			tmpSurvey.put(FieldName.TITLE, title);
		}

		tmpSurvey.put(FieldName.UPDATETIME, new Date().getTime());
		updateSurveyData(surveyID, tmpSurvey);

	}

	public void updateSurveyData(String _id, JsonObject newData) {
		this.updateDocument(new JsonObject().put(FieldName._ID, _id), newData, null, handler -> {
			if (handler.succeeded()) {
				this.retrieveSurvey(new JsonObject().put(FieldName._ID, _id), h -> {
					this.CompleteGenerateResponse(CodeMapping.C0000.toString(), "Updated", h.result().get(0));
				});
			} else {
				this.CompleteGenerateResponse(CodeMapping.S1111.toString(), CodeMapping.S1111.value(), null);
			}
		});
	}

	// Check
	public void CheckPermisstionDoing(String username, String surveyID) {
		UserDao lvUserDao = new UserDao();
		lvUserDao.doGetUserInfobyUserName(username);
		this.queryDocument(new JsonObject().put(FieldName._ID, surveyID), handler -> {
			if (handler.succeeded() && handler.result() != null) {
				JsonObject surveyData = handler.result().get(0);
				if (!surveyData.getString(FieldName.STATE).equals("A")
						|| !surveyData.getString(FieldName.STATUS).equals("N")
						|| surveyData.getString(FieldName.USERNAME).equals(username)) {
					this.CompleteGenerateResponse(CodeMapping.S6666.toString(), CodeMapping.S6666.value(), surveyData);
					return;
				}
				// String settingid = surveyData.getString(FieldName.SETTINGID);
				JsonObject setting = surveyData.getJsonObject(FieldName.SETTING);
				// check all fields Setting
				// JsonObject userData =
				// lvUserDao.getMvFutureResponse().result().getJsonObject(FieldName.DATA);
				// Check end Date
				boolean checkdate = false;
				checkdate = setting.getLong(FieldName.ENDTIME) == null ? false
						: (new Timestamp(setting.getLong(FieldName.ENDTIME))).before(new Date());
				if (checkdate) {
					// Expired
					this.CompleteGenerateResponse(CodeMapping.S3333.toString(), CodeMapping.S3333.value(), null);
				} else {
					if (setting.getBoolean(FieldName.ALLOWMULTIRESPONSE) == null ? true
							: setting.getBoolean(FieldName.ALLOWMULTIRESPONSE)) {
						if (setting.getBoolean(FieldName.ISPUBLIC) == null ? true
								: setting.getBoolean(FieldName.ISPUBLIC)) {
							completeGetAllSurveyData(surveyData, CodeMapping.S5555);
						} else {
							io.vertx.core.json.JsonArray lstUserName = setting.getJsonArray(FieldName.LISTALLOWUSER);
							if (lstUserName == null) {
								lstUserName = new JsonArray();
							}
							if (lstUserName.contains(username)) {
								completeGetAllSurveyData(surveyData, CodeMapping.S5555);
							} else {
								this.CompleteGenerateResponse(CodeMapping.S2222.toString(), CodeMapping.S2222.value(),
										null);
							}
						}
					} else {
						SurveySubmitDao lvSurveySubmitDao = new SurveySubmitDao();
						lvSurveySubmitDao.queryDocument(
								new JsonObject().put(FieldName.SURVEYID, surveyID).put(FieldName.USERNAME, username),
								handlerSub -> {
									if (handlerSub.succeeded() && handlerSub.result() != null) {
										if (handlerSub.result().size() > 0) {
											this.CompleteGenerateResponse(CodeMapping.S4444.toString(),
													"Multi response is deny", null);
										} else {
											if (setting.getBoolean(FieldName.ISPUBLIC) == null ? true
													: setting.getBoolean(FieldName.ISPUBLIC)) {
												completeGetAllSurveyData(surveyData, CodeMapping.S5555);
											} else {
												io.vertx.core.json.JsonArray lstUserName = setting
														.getJsonArray(FieldName.LISTALLOWUSER);
												if (lstUserName == null) {
													lstUserName = new JsonArray();
												}
												if (lstUserName.contains(username)) {
													completeGetAllSurveyData(surveyData, CodeMapping.S5555);
												} else {
													this.CompleteGenerateResponse(CodeMapping.S2222.toString(),
															CodeMapping.S2222.value(), null);
												}
											}
											// completeGetAllSurveyData(surveyData, CodeMapping.S5555);
										}
									} else {
										if (setting.getBoolean(FieldName.ISPUBLIC) == null ? true
												: setting.getBoolean(FieldName.ISPUBLIC)) {
											completeGetAllSurveyData(surveyData, CodeMapping.S5555);
										} else {
											io.vertx.core.json.JsonArray lstUserName = setting
													.getJsonArray(FieldName.LISTALLOWUSER);
											if (lstUserName == null) {
												lstUserName = new JsonArray();
											}
											if (lstUserName.contains(username)) {
												completeGetAllSurveyData(surveyData, CodeMapping.S5555);
											} else {
												this.CompleteGenerateResponse(CodeMapping.S2222.toString(),
														CodeMapping.S2222.value(), null);
											}
										}
										// completeGetAllSurveyData(surveyData, CodeMapping.S5555);
									}
								});
					}
				}

			} else {
				this.CompleteGenerateResponse(CodeMapping.S1111.toString(), CodeMapping.S1111.toString(), null);
			}
		});
	}

	private void completeGetAllSurveyData(JsonObject surveyData, CodeMapping pCode) {
		this.CompleteGenerateResponse(CodeMapping.C0000.toString(), CodeMapping.C0000.value(), surveyData);
	}

	public void pushlishSurvey(String surveyID, String username, double limitResp, float pointPerOne, float initialFund,
			boolean noti, float limitFund) {
		this.queryDocument(new JsonObject().put(FieldName._ID, surveyID), handler -> {
			if (handler.succeeded() && handler.result() != null) {
				if (handler.result().get(0) == null) {
					this.CompleteGenerateResponse(CodeMapping.S1111.toString(), CodeMapping.S1111.toString(), null);
				} else {
					JsonObject surveyData = handler.result().get(0);
					if (surveyData.getString(FieldName.USERNAME).equals(username)) {
						if (!surveyData.getString(FieldName.STATE).equals("A")) {
							this.CompleteGenerateResponse(CodeMapping.S8888.toString(), CodeMapping.S8888.value(),
									null);
						} else {
							if (surveyData.getString(FieldName.STATUS).equals("N")) {
								// Survey đã dc pushlish - > reject yêu cầu pushlish nếu muốn cập nhật tăng số tiền thì có thể nộp tiền hoặc rút tiền
								this.CompleteGenerateResponse(CodeMapping.S9999.toString(), CodeMapping.S9999.value(), null);
							} else {
								// Tao moi thong tin pushlish
								SurveyPushlishDao lvSurveyPushlishDao = new SurveyPushlishDao();
								lvSurveyPushlishDao.newPushlishAction(surveyID, limitResp, pointPerOne, initialFund,
										noti, limitFund).setHandler(push -> {
											if (push.succeeded()) {
												this.updateSurveyData(surveyID,
														new JsonObject().put(FieldName.STATUS, "P")
																.put(FieldName.PUSHLISHDATE, new Date().getTime()));
											} else {
												this.CompleteGenerateResponse(CodeMapping.C3333.toString(),
														CodeMapping.C3333.value(), null);
											}
										});
							}
						}
					} else {
						this.CompleteGenerateResponse(CodeMapping.S2222.toString(), CodeMapping.S2222.value(), null);
					}
				}
			} else {
				this.CompleteGenerateResponse(CodeMapping.S1111.toString(), CodeMapping.S1111.toString(), null);
			}
		});
	}

	public void closesurvey(String username, String userID, String surveyID, boolean isStop, String remark) {
		this.queryDocument(new JsonObject().put(FieldName._ID, surveyID).put(FieldName.USERNAME, username), handler -> {
			if (handler.succeeded() & handler.result() != null) {
				if (isStop) {
					JsonObject close = new JsonObject();
					close.put(FieldName.STATUS, "C");
					close.put(FieldName.REMARK, remark);
					this.updateSurveyData(surveyID, close);
					// Send remain money to ethe Server
					Future<JsonObject> response = Future.future();
					VertxServiceCenter.getInstance().sendNewMessage(
							EventBusDiscoveryConst.ETHEREUMPROXYDISCOVERY.name(),
							new JsonObject().put(FieldName.SURVEYID, surveyID).put(FieldName.USERNAME, username)
									.put(FieldName.ACTION, "closesurvey"),
							response);
					response.setHandler(handler1 -> {
						ProxyLogDao lvDao = new ProxyLogDao();
						lvDao.storeNewRequest(
								"closesurvey", new JsonObject().put(FieldName.SURVEYID, surveyID)
										.put(FieldName.USERNAME, username).put(FieldName.ACTION, "closesurvey"),
								handler1.result());
					});
				} else {
					JsonObject pause = new JsonObject();
					pause.put(FieldName.STATE, "S");
					pause.put(FieldName.REMARK, remark);
					this.updateSurveyData(surveyID, pause);
				}
			} else {
				this.CompleteGenerateResponse(CodeMapping.S1111.toString(), "Survey not found or permission deny",
						null);
			}
		});
	}

	public void retriveAndCountTotalResponseData(String username, JsonObject query, JsonObject project) {
		JsonObject command = new JsonObject();
		JsonArray pipeline = new JsonArray();
		pipeline.add(new JsonObject().put("$match", query));
		if (project != null) {
			pipeline.add(new JsonObject().put("$project", project));
		}
		pipeline.add(new JsonObject().put("$sort", new JsonObject().put(FieldName.INPUTTIME, -1)));
		pipeline.add(new JsonObject().put("$lookup", new JsonObject().put("from", "survey_response")
				.put("localField", "_id").put("foreignField", FieldName.SURVEYID).put("as", "response")));
		pipeline.add(new JsonObject().put("$lookup", new JsonObject().put("from", SurveyPushlishDao.collectionname)
				.put("localField", "_id").put("foreignField", FieldName.SURVEYID).put("as", "pushlish")));

		command.put("aggregate", this.getCollectionName());
		command.put("cursor", new JsonObject().put("batchSize", 1000));
		command.put("pipeline", pipeline);
		BaseDaoConnection.getInstance().getMongoClient().runCommand("aggregate", command, resultHandler -> {
			if (resultHandler.succeeded()) {
				JsonArray result = resultHandler.result().getJsonObject("cursor").getJsonArray("firstBatch");
				if (result != null) {
					for (int i = 0; i < result.size(); i++) {
						if (result.getJsonObject(i).getString(FieldName.USERNAME).equals(username)) {
							result.getJsonObject(i).put(FieldName.ENABLEEDIT, true);
							result.getJsonObject(i).put(FieldName.ENABLESUBMIT, false);
						}
						result.getJsonObject(i).put("totalresponse",
								result.getJsonObject(i).getJsonArray("response").size());
						result.getJsonObject(i).remove("response");
					}
					this.CompleteGenerateResponse(CodeMapping.C0000.toString(), CodeMapping.C0000.value(), result);
				} else {
					this.CompleteGenerateResponse(CodeMapping.S1111.toString(), CodeMapping.S1111.value(), null);
				}
			} else {
				this.CompleteGenerateResponse(CodeMapping.S1111.toString(), CodeMapping.S1111.value(),
						resultHandler.cause().getMessage());
			}

		});
	}

	public Future<Long> retriveCountTotalResponseData(JsonObject query) {
		JsonArray pipeline = new JsonArray();
		Future<Long> lvResult = Future.future();
		pipeline.add(new JsonObject().put("$match", query));
		BaseDaoConnection.getInstance().getMongoClient().count(getCollectionName(), query, resultHandler -> {
			lvResult.complete(resultHandler.result());
		});
		return lvResult;
	}
}
