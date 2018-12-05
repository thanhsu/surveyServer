package com.survey.dbservice.dao;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.survey.constant.EventBusDiscoveryConst;
import com.survey.constant.UserNotificationEnum;
import com.survey.etheaction.ProxyCreateSurvey;
import com.survey.notification.actions.NotifiSurveyStatusUpdate;
import com.survey.utils.CodeMapping;
import com.survey.utils.FieldName;
import com.survey.utils.PushMessageBean;
import com.survey.utils.VertxServiceCenter;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.UpdateOptions;
import sun.tools.tree.IfStatement;

public class SurveyDao extends SurveyBaseDao {
	public static final String SurveyCollectionName = "survey_datas";
	public static final String SurveyCollectionNameBK = "survey_datas_bk";

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
		tmpSurvey.put(FieldName.LISTCATEGORYID, categoryID);
		tmpSurvey.put(FieldName.STATE, "A");
		tmpSurvey.put(FieldName.STATUS, "L");
		tmpSurvey.put(FieldName.INPUTTIME, new Date().getTime());
		
		JsonObject tmpSetting = new JsonObject();
		tmpSetting.put(FieldName.LOGINREQUIRE, false);
		tmpSetting.put(FieldName.FAVOURITE_ENABLE, false);
		tmpSetting.put(FieldName.ALLOWMULTIRESPONSE, false);
		tmpSetting.put(FieldName.ISPUBLIC, true);
		tmpSetting.put(FieldName.ENDLESS, true);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String strDate = formatter.format(new Date());
		tmpSetting.put(FieldName.STARTDATE, strDate);
		tmpSetting.put(FieldName.TITLE, title);
		tmpSetting.put(FieldName.DESCRIPTION, description);
		tmpSetting.put(FieldName.ENABLEDIRECTURL, false);
		
		tmpSurvey.put(FieldName.SETTING, tmpSetting);
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
			JsonObject theme, String title, String description) {
		JsonObject tmpSurvey = new JsonObject();
		if (questionData != null) {
			tmpSurvey.put(FieldName.QUESTIONDATA, questionData);
		}
		if (settingID != null) {
			tmpSurvey.put(FieldName.SETTING, settingID);
			if (settingID.getBoolean(FieldName.FAVOURITE_ENABLE) == null) {
				settingID.put(FieldName.FAVOURITE_ENABLE, false);
			}
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
		if (description != null) {
			tmpSurvey.put(FieldName.DESCRIPTION, description);
		}

		tmpSurvey.put(FieldName.UPDATETIME, new Date().getTime());
		updateSurveyData(surveyID, tmpSurvey);

	}

	public void updateSurveyData(String _id, JsonObject newData) {
		this.updateDocument(new JsonObject().put(FieldName._ID, _id), newData, new UpdateOptions(false), handler -> {
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
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				if (sdf.format(new Date()).compareTo(setting.getString(FieldName.STARTDATE)) < 0) {
					checkdate = true;
				} else {
					if (!(setting.getBoolean(FieldName.ENDLESS) == null ? true
							: setting.getBoolean(FieldName.ENDLESS))) {
						if (sdf.format(new Date()).compareTo(setting.getString(FieldName.ENDDATE)) > 0) {
							checkdate = true;
						}
					}
				}
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

	// New check permission
	public void CheckPermisstionDoing(String username, String surveyID, JsonObject loginData) {
		UserDao lvUserDao = new UserDao();
		lvUserDao.doGetUserInfobyUserName(username);
		this.queryDocument(new JsonObject().put(FieldName._ID, surveyID), handler -> {
			if (handler.succeeded() && handler.result() != null) {
				JsonObject surveyData = handler.result().get(0);
				if (!surveyData.getString(FieldName.STATE).equals("A")
						|| !surveyData.getString(FieldName.STATUS).equals("N")
						|| surveyData.getString(FieldName.USERNAME).equals(username)) {
					// not pushlish or blabla
					surveyData.put(FieldName.QUESTIONDATA, new JsonObject());
					this.CompleteGenerateResponse(CodeMapping.S6666.toString(), CodeMapping.S6666.value(), surveyData);
					return;
				}

				JsonObject setting = surveyData.getJsonObject(FieldName.SETTING);
				// Check date
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				String now = sdf.format(new Date());
				boolean isStart = now.compareTo(setting.getString(FieldName.STARTDATE)) >= 0;
				boolean isEnd = false;

				if (isStart) {
					boolean isEndless = setting.getBoolean(FieldName.ENDLESS) == null ? true
							: setting.getBoolean(FieldName.ENDLESS);
					if (!isEndless) {
						isEnd = now.compareTo(setting.getString(FieldName.ENDDATE)) > 0;
					}
				}
				if (!isStart) {
					surveyData.put(FieldName.QUESTIONDATA, new JsonObject());
					this.CompleteGenerateResponse(CodeMapping.S3334.toString(), CodeMapping.S3334.value(), surveyData);
				} else if (isEnd) {
					surveyData.put(FieldName.QUESTIONDATA, new JsonObject());
					this.CompleteGenerateResponse(CodeMapping.S3333.toString(), CodeMapping.S3333.value(), surveyData);
				} else {
					boolean isAllowMulti = setting.getBoolean(FieldName.ALLOWMULTIRESPONSE) == null ? true
							: setting.getBoolean(FieldName.ALLOWMULTIRESPONSE);
					boolean isPublic = setting.getBoolean(FieldName.ISPUBLIC) == null ? true
							: setting.getBoolean(FieldName.ISPUBLIC);
					boolean isLoginRequire = setting.getBoolean(FieldName.ISPUBLIC) == null ? true
							: setting.getBoolean(FieldName.LOGINREQUIRE);

					if (isLoginRequire) {
						// check session
						if (loginData != null) {
							if (loginData.getString("username").equals(username)) {
								completeGetAllSurveyData(surveyData, CodeMapping.S5555);
							} else {
								surveyData.put(FieldName.QUESTIONDATA, new JsonObject());
								this.CompleteGenerateResponse(CodeMapping.S0002.toString(), CodeMapping.S0002.value(),
										surveyData);
							}
						} else {
							surveyData.put(FieldName.QUESTIONDATA, new JsonObject());
							this.CompleteGenerateResponse(CodeMapping.S0002.toString(), CodeMapping.S0002.value(),
									surveyData);
						}
					} else if (isAllowMulti) {
						if (isPublic) {
							completeGetAllSurveyData(surveyData, CodeMapping.S5555);
						} else {
							String lstUserName = setting.getString(FieldName.LISTALLOWUSER);
							if (lstUserName == null || lstUserName.isEmpty()) {
								completeGetAllSurveyData(surveyData, CodeMapping.S5555);
							} else {
								if (lstUserName.contains(username)) {
									completeGetAllSurveyData(surveyData, CodeMapping.S5555);
								} else {
									surveyData.put(FieldName.QUESTIONDATA, new JsonObject());
									this.CompleteGenerateResponse(CodeMapping.S2222.toString(),
											CodeMapping.S2222.value(), surveyData);
								}
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
											if (isPublic) {
												completeGetAllSurveyData(surveyData, CodeMapping.S5555);
											} else {
												String lstUserName = setting.getString(FieldName.LISTALLOWUSER);
												if (lstUserName == null || lstUserName.isEmpty()) {
													completeGetAllSurveyData(surveyData, CodeMapping.S5555);
												} else {
													if (lstUserName.contains(username)) {
														completeGetAllSurveyData(surveyData, CodeMapping.S5555);
													} else {
														surveyData.put(FieldName.QUESTIONDATA, new JsonObject());
														this.CompleteGenerateResponse(CodeMapping.S2222.toString(),
																CodeMapping.S2222.value(), surveyData);
													}
												}
											}
											// completeGetAllSurveyData(surveyData, CodeMapping.S5555);
										}
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

	public Future<JsonObject> retrieveSurveyStatus(String surveyID) {
		Future<JsonObject> lvResult = Future.future();
		Future<JsonArray> lvTmp = Future.future();
		lvTmp.setHandler(h -> {
			if (h.succeeded()) {
				lvResult.complete(h.result().getJsonObject(0));
			}
		});
		this.queryDocumentRunCmd(new JsonObject().put(FieldName._ID, surveyID), new JsonObject()
				.put(FieldName.QUESTIONDATA, 0).put(FieldName.LISTCATEGORYID, 0).put(FieldName.SETTING, 0),
				new JsonObject(), lvTmp);
		return lvResult;
	}

	public void pushlishSurvey(String surveyID, String username, double limitResp, double pointPerOne, double initialFund,
			boolean noti, double limitFund) {
		this.queryDocument(new JsonObject().put(FieldName._ID, surveyID), handler -> {
			if (handler.succeeded() && handler.result() != null) {
				if (handler.result().isEmpty()) {
					this.CompleteGenerateResponse(CodeMapping.S1111.toString(), CodeMapping.S1111.value(), null);
				} else {
					JsonObject surveyData = handler.result().get(0);
					if (surveyData.getString(FieldName.USERNAME).equals(username)) {
						if (!surveyData.getString(FieldName.STATE).equals("A")) {
							this.CompleteGenerateResponse(CodeMapping.S8888.toString(), CodeMapping.S8888.value(),
									null);
						} else {
							if (surveyData.getString(FieldName.STATUS).equals("N")) {
								// Survey đã dc pushlish - > reject yêu cầu pushlish nếu muốn cập nhật tăng số
								// tiền thì có thể nộp tiền hoặc rút tiền
								this.CompleteGenerateResponse(CodeMapping.S9999.toString(), CodeMapping.S9999.value(),
										null);
							} else {
								// Tao moi thong tin pushlish
								SurveyPushlishDao lvSurveyPushlishDao = new SurveyPushlishDao();
								lvSurveyPushlishDao.newPushlishAction(surveyID, limitResp, pointPerOne, initialFund,
										noti, limitFund).setHandler(push -> {
											if (push.succeeded()) {
												ProxyCreateSurvey lvProxyCreateSurvey = new ProxyCreateSurvey(surveyID, username, initialFund, limitFund, noti, pointPerOne, limitResp, push.result());
												lvProxyCreateSurvey.sendToProxyServer().setHandler(h2->{
													if(h2.succeeded()&&h2.result()!=null) {
														if(h2.result().getString(FieldName.CODE).equals("P0000")) {
															this.updateSurveyData(surveyID,
																	new JsonObject().put(FieldName.STATUS, "P")
																			.put(FieldName.PUSHLISHDATE, new Date().getTime()));
														}else {
															this.CompleteGenerateResponse(CodeMapping.P2222.toString(),
																	CodeMapping.P2222.value(), h2.result());
														}
													}else {
														this.CompleteGenerateResponse(CodeMapping.C1111.toString(),
																CodeMapping.C1111.value(), null);
													}
												});
												
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

	public void closesurvey(String username, String surveyID, boolean isStop, String remark) {
		this.queryDocument(new JsonObject().put(FieldName._ID, surveyID).put(FieldName.USERNAME, username), handler -> {
			if (handler.succeeded() && handler.result() != null && !handler.result().isEmpty()) {
				JsonObject lvSurveyNewStatus = new JsonObject();

				if (isStop) {

					lvSurveyNewStatus.put(FieldName.STATE, "C");
					lvSurveyNewStatus.put(FieldName.REMARK, remark);
					this.updateSurveyData(surveyID, lvSurveyNewStatus);
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

					lvSurveyNewStatus.put(FieldName.STATE, "S");
					lvSurveyNewStatus.put(FieldName.REMARK, remark);
					this.updateSurveyData(surveyID, lvSurveyNewStatus);
				}
				// Send notification

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

		pipeline.add(new JsonObject().put("$lookup",
				new JsonObject().put("from", SurveyCategoryDao.SurveyCategoryCollection)
						.put("localField", FieldName.LISTCATEGORYID).put("foreignField", "categoryID")
						.put("as", "categorydetail")));

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
						if (result.getJsonObject(i).getJsonArray(FieldName.QUESTIONDATA) != null) {
							result.getJsonObject(i).put(FieldName.TOTALQUESTION,
									result.getJsonObject(i).getJsonArray(FieldName.QUESTIONDATA).size());
						} else {
							result.getJsonObject(i).put(FieldName.TOTALQUESTION, 0);
						}
						result.getJsonObject(i).remove(FieldName.QUESTIONDATA);
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

	public void retriveAndCountTotalResponseData(String username, JsonObject query, JsonObject project,
			boolean detail) {
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

		pipeline.add(new JsonObject().put("$lookup",
				new JsonObject().put("from", SurveyCategoryDao.SurveyCategoryCollection)
						.put("localField", FieldName.LISTCATEGORYID).put("foreignField", "categoryID")
						.put("as", "categorydetail")));

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
						if (result.getJsonObject(i).getJsonArray(FieldName.QUESTIONDATA) != null) {
							result.getJsonObject(i).put(FieldName.TOTALQUESTION,
									result.getJsonObject(i).getJsonArray(FieldName.QUESTIONDATA).size());
						} else {
							result.getJsonObject(i).put(FieldName.TOTALQUESTION, 0);
						}
						if (!detail) {
							result.getJsonObject(i).remove(FieldName.QUESTIONDATA);
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

	public Future<String> copyTempSurvey(String username, String tempID) {
		Future<String> newSurveyID = Future.future();
		this.findOneByID(tempID).setHandler(handler -> {
			if (handler.succeeded() && handler.result() != null) {
				JsonObject tmp = handler.result();
				if (tmp.getBoolean(FieldName.ISTEMP)) {
					tmp.remove(FieldName._ID);
					tmp.put(FieldName.ISTEMP, false);
					tmp.put(FieldName.USERNAME, username);
					tmp.put(FieldName.STATE, "A");
					tmp.put(FieldName.STATUS, "L");
					this.saveDocumentReturnID(tmp).setHandler(newSv -> {
						newSurveyID.complete(newSv.result());
					});
				} else if (tmp.getJsonObject(FieldName.SETTING).getBoolean(FieldName.FAVOURITE_ENABLE) == null ? false
						: tmp.getJsonObject(FieldName.SETTING).getBoolean(FieldName.FAVOURITE_ENABLE)) {
					tmp.remove(FieldName._ID);
					tmp.put(FieldName.ISTEMP, false);
					tmp.put(FieldName.USERNAME, username);
					tmp.put(FieldName.STATE, "A");
					tmp.put(FieldName.STATUS, "L");
					this.saveDocumentReturnID(tmp).setHandler(newSv -> {
						newSurveyID.complete(newSv.result());
					});
				} else {
					newSurveyID.fail("Source Survey is not Template");
				}
			} else {
				newSurveyID.complete(null);
			}
		});
		return newSurveyID;
	}

	public void restoreDisableSurvey(String surveyID, String username) {
		this.queryDocument(new JsonObject().put(FieldName._ID, surveyID).put(FieldName.USERNAME, username), handler -> {
			if (handler.succeeded() & handler.result() != null) {
				// check State if state == C do enable
				if (handler.result().get(0).getString(FieldName.STATE).equals("C")) {
					this.updateDocument(new JsonObject().put(FieldName._ID, surveyID),
							new JsonObject().put(FieldName.STATE, "A"), new UpdateOptions(), handler2 -> {
								this.retrieveSurvey(new JsonObject().put(FieldName._ID, surveyID), h -> {
									this.CompleteGenerateResponse(CodeMapping.C0000.toString(), "", h.result().get(0));
								});
							});
				} else {
					this.CompleteGenerateResponse(CodeMapping.S0001.toString(), CodeMapping.S0001.value(),
							handler.result().get(0));
				}
			}
		});
	}

	public Future<Void> deleteSurvey(String username, String surveyID, String remark) {
		Future<Void> deleteResult = Future.future();
		/*
		 * JsonObject del = new JsonObject(); del.put(FieldName.STATE, "D");
		 * del.put(FieldName.DELETEREMARK, remark);
		 */
		this.queryDocument(new JsonObject().put(FieldName.USERNAME, username).put(FieldName._ID, surveyID)
				.put(FieldName.STATUS, new JsonObject().put("$ne", "N")), handler -> {
					if (handler.succeeded() & handler.result() != null) {
						if (handler.result().size() > 0) {
							JsonObject tmp = handler.result().get(0);
							tmp.put(FieldName.STATE, "D");
							this.delteDocument(new JsonObject().put(FieldName._ID, surveyID), handler2 -> {
								BaseDaoConnection.getInstance().getMongoClient().save(SurveyCollectionNameBK, tmp,
										handler4 -> {
											if (handler4.succeeded()) {
												deleteResult.complete();
											} else {
												deleteResult.fail(handler4.cause());
											}
										});
							});
						}
					} else {
						deleteResult.fail(handler.cause().getMessage());
					}
				});
		return deleteResult;
	}

	public Future<List<JsonObject>> retrieveSurveyPushlished(String username) {
		 Future<List<JsonObject>> handler = Future.future();
		JsonObject query = new JsonObject();
		query.put(FieldName.USERNAME, username);
		query.put(FieldName.STATE, new JsonObject().put("$in", new JsonArray().add("A").add("C")));
		query.put(FieldName.STATUS, new JsonObject().put("$in", new JsonArray().add("N").add("P")));
		this.queryDocument(query, handler);
		return handler;
	}

	private void sendNotification(String surveyID, boolean isProvate, boolean isPublic) {
		NotifiSurveyStatusUpdate lvStatusUpdate = new NotifiSurveyStatusUpdate();
		lvStatusUpdate.setSurveyID(surveyID);
		lvStatusUpdate.setPrivate(isProvate);
		lvStatusUpdate.setPublic(isPublic);
		lvStatusUpdate.generate();
	}
}
