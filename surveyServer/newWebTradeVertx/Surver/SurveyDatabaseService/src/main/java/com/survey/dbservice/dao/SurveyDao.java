package com.survey.dbservice.dao;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import com.survey.utils.CodeMapping;
import com.survey.utils.FieldName;

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

	public Future<String> createSurvey(String username, String title, String categoryID) {
		JsonObject tmpSurvey = new JsonObject();
		tmpSurvey.put(FieldName.USERNAME, username);
		tmpSurvey.put(FieldName.TITLE, title);
		tmpSurvey.put(FieldName.ISTEMP, false);
		tmpSurvey.put(FieldName.STATE, "A");
		tmpSurvey.put(FieldName.STATUS, "L");
		tmpSurvey.put(FieldName.INPUTTIME, new JsonObject().put("$date", new Timestamp(new Date().getTime())));
		return this.saveDocumentReturnID(tmpSurvey);
	}

	public Future<String> newSurvey(String username, String title, JsonObject category, JsonArray questionData,
			JsonObject setting, String ruleData, String themeId) {
		JsonObject tmpSurvey = new JsonObject();
		tmpSurvey.put(FieldName.USERNAME, username);
		tmpSurvey.put(FieldName.TITLE, title);
		tmpSurvey.put(FieldName.CATEGORY, category);
		tmpSurvey.put(FieldName.QUESTIONDATA, questionData);
		tmpSurvey.put(FieldName.STATE, "A");
		tmpSurvey.put(FieldName.STATUS, "L");
		tmpSurvey.put(FieldName.SETTING, setting);
		tmpSurvey.put(FieldName.THEMEID, themeId);

		tmpSurvey.put(FieldName.INPUTTIME, new JsonObject().put("$date", new Timestamp(new Date().getTime())));
		return this.saveDocumentReturnID(tmpSurvey);
	}

	public void UpdateSurvey(String surveyID, JsonArray questionData, JsonObject settingID, JsonObject ruleData,
			JsonObject theme) {
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
			tmpSurvey.put(FieldName.RULEDATA, ruleData);
		}

		tmpSurvey.put(FieldName.UPDATETIME, new JsonObject().put("$date", new Timestamp(new Date().getTime())));
		updateSurveyData(surveyID, tmpSurvey);

	}

	public void updateSurveyData(String _id, JsonObject newData) {
		this.updateDocument(new JsonObject().put(FieldName._ID, _id), newData, null, handler -> {
			if (handler.succeeded()) {
				this.CompleteGenerateResponse(CodeMapping.C0000.toString(), "Updated", null);
			} else {
				this.CompleteGenerateResponse(CodeMapping.S1111.toString(), CodeMapping.S1111.value(), null);
			}
		});
	}

	public void activeServey(String id) {

	}

	// Check
	public void CheckPermisstionDoing(String username, String surveyID) {
		UserDao lvUserDao = new UserDao();
		lvUserDao.doGetUserInfobyUserName(username);

		this.queryDocument(new JsonObject().put(FieldName._ID, surveyID), handler -> {
			if (handler.succeeded() && handler.result() != null) {
				JsonObject surveyData = handler.result().get(0);
				if (!surveyData.getString(FieldName.STATE).equals("A")
						|| !surveyData.getString(FieldName.STATUS).equals("N")) {
					this.CompleteGenerateResponse(CodeMapping.S6666.toString(), CodeMapping.S6666.value(), surveyData);
					return;
				}
				String settingid = surveyData.getString(FieldName.SETTINGID);
				SurveySettingDao lvSettingDao = new SurveySettingDao();
				lvSettingDao.retrieveSetting(settingid).setHandler(setting -> {
					if (setting.succeeded()) {
						// check all fields Setting
						JsonObject userData = lvUserDao.getMvFutureResponse().result().getJsonObject(FieldName.DATA);
						// Check public
						if (setting.result().getBoolean(FieldName.ISPUBLIC)) {
							// Check end Date
							Timestamp lvEndDate = new Timestamp(setting.result().getLong(FieldName.ENDTIME));
							if (lvEndDate.before(new Date())) {
								// Expired
								this.CompleteGenerateResponse(CodeMapping.S3333.toString(), CodeMapping.S3333.value(),
										null);
							} else {
								if (setting.result().getBoolean(FieldName.ALLOWMULTIRESPONSE)) {
									completeGetAllSurveyData(surveyData, CodeMapping.S5555);
								} else {
									SurveySubmitDao lvSurveySubmitDao = new SurveySubmitDao();
									lvSurveySubmitDao.queryDocument(new JsonObject().put(FieldName.SURVEYID, surveyID)
											.put(FieldName.USERNAME, username), handlerSub -> {
												if (handlerSub.succeeded() && handlerSub.result() != null) {
													if (handlerSub.result().size() > 0) {
														this.CompleteGenerateResponse(CodeMapping.S4444.toString(),
																"Multi response is deny", null);
													} else {
														completeGetAllSurveyData(surveyData, CodeMapping.S5555);
													}
												} else {
													completeGetAllSurveyData(surveyData, CodeMapping.S5555);
												}
											});
								}
							}
						} else {
							// Check with list user enable
							io.vertx.core.json.JsonArray lstUserName = setting.result()
									.getJsonArray(FieldName.LISTALLOWUSER);
							if (lstUserName.contains(username)) {
								completeGetAllSurveyData(surveyData, CodeMapping.S5555);
							}
						}
					} else {
						// Not Settin only check username
						if (surveyData.getString(FieldName.USERNAME).equals(username)) {
							this.CompleteGenerateResponse(CodeMapping.S2222.toString(), "Permisstion Deny", surveyData);
						} else {
							completeGetAllSurveyData(surveyData, CodeMapping.S5555);
						}
					}
				});
			} else {
				this.CompleteGenerateResponse(CodeMapping.S1111.toString(), "Survey Not Found!", null);
			}
		});
	}

	private void completeGetAllSurveyData(JsonObject surveyData, CodeMapping pCode) {
		String settingID = surveyData.getString(FieldName.SETTINGID);
		String categoryID = surveyData.getString(FieldName.CATEGORYID);
		String themeID = surveyData.getString(FieldName.THEMEID);
		Future<JsonObject> lvResult = Future.future();
		lvResult.map(surveyData);
		SurveySettingDao lvSettingDao = new SurveySettingDao();
		lvSettingDao.retrieveSetting(settingID).setHandler(handler -> {
			if (handler.succeeded()) {
				lvResult.map(lvResult.result().put(FieldName.SETTING, handler.result()));
			}
		});

		SurveyCategoryDao lvCategoryDao = new SurveyCategoryDao();
		lvCategoryDao.findOneByID(categoryID).setHandler(handler -> {
			if (handler.succeeded()) {
				lvResult.map(lvResult.result().put(FieldName.CATEGORY, handler.result()));
			}
		});

		SurveyThemeDao lvSurveyThemeDao = new SurveyThemeDao();
		lvSurveyThemeDao.retrieveTheme(themeID).setHandler(handler -> {
			if (handler.succeeded()) {
				lvResult.map(lvResult.result().put(FieldName.THEME, handler.result()));
			}
		});

		lvResult.setHandler(handler -> {
			this.CompleteGenerateResponse(pCode.toString(), pCode.value(), handler.result());
		});
	}
}
