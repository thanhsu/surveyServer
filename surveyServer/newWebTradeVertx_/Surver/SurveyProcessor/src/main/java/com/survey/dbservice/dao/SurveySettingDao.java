package com.survey.dbservice.dao;

import java.io.File;

import com.survey.utils.FieldName;
import com.survey.utils.Log;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

public class SurveySettingDao extends SurveyBaseDao {
	public static final String SurveySettingCollectionName = "survey_setting";

	public SurveySettingDao() {
		setCollectionName(SurveySettingCollectionName);
	}

	public Future<String> saveNewSetting(JsonObject setting) {
		return this.saveDocumentReturnID(setting);
	}

	public void updateSetting(String id, JsonObject newData) {
		this.updateDocument(new JsonObject().put(FieldName._ID, id), newData, null, handler -> {
			Log.print("Update Success");
		});
	}

	public Future<JsonObject> retrieveSetting(String id) {
		Future<JsonObject> lvResult = Future.future();
		this.queryDocument(new JsonObject().put(FieldName._ID, id), handler -> {
			if (handler.succeeded() && handler.result() != null) {
				lvResult.complete(handler.result().get(0));
			} else {
				lvResult.fail("Null");
			}
		});
		return lvResult;
	}
}
