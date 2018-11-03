package com.survey.dbservice.dao;

import java.sql.Timestamp;
import java.util.Date;

import com.survey.utils.FieldName;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

public class SurveyThemeDao extends SurveyBaseDao {
	public static final String SurveyThemeCollection = "survey_theme";

	public SurveyThemeDao() {
		setCollectionName(SurveyThemeCollection);
	}

	public void createNewTheme(JsonObject theme) {
		theme.put(FieldName.INPUTTIME, new JsonObject().put("$date", new Timestamp(new Date().getTime())));
		this.saveDocumentReturnID(theme);
	}

	public void updateTheme(String id, JsonObject data) {
		this.updateDocument(new JsonObject().put(FieldName._ID, id), data, null, handler -> {
		});
	}

	public Future<JsonObject> retrieveTheme(String id) {
		Future<JsonObject> lvResult = Future.future();
		this.queryDocument(new JsonObject().put(FieldName._ID, id), handler -> {
			if (handler.succeeded() && handler.result() != null) {
				lvResult.complete(handler.result().get(0));
			} else {
				lvResult.fail("null");
			}
		});
		return lvResult;
	}
}
