package com.survey.dbservice.dao;

import com.survey.utils.CodeMapping;
import com.survey.utils.FieldName;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class SurveySubmitDao extends SurveyBaseDao {
	public static final String SurveyResponseCollectionName = "survey_response";

	public SurveySubmitDao() {
		setCollectionName(SurveyResponseCollectionName);
	}

	public void newSurveyResult(String username, JsonObject data, String surveyID) {
		JsonObject lvData = new JsonObject().put(FieldName.SURVEYID, surveyID);
		lvData.put(FieldName.USERNAME, username);
		lvData.put(FieldName.DATA, data);
		this.saveDocument(lvData);
	}

	public void retrieveAllSubmitResult(String surveyID) {
		this.queryDocument(new JsonObject().put(FieldName.SURVEYID, surveyID), handler -> {
			this.CompleteGenerateResponse(CodeMapping.C0000.toString(), "", handler.result());
		});
	}

	public void retrieveAllSurveyComplete(String userID) {
		this.queryDocument(new JsonObject().put(FieldName.USERID, userID), handler -> {
			this.CompleteGenerateResponse(CodeMapping.C0000.toString(), "", handler.result());
		});
	}

	public void getAllServeyDone(String username) {
		this.queryDocument(new JsonObject().put(FieldName.USERID, username), handler -> {
			if (handler.succeeded() && handler.result() != null) {
				JsonArray lst = new JsonArray();
				for (int i = 0; i < handler.result().size(); i++) {
					lst.add(handler.result().get(i).getString(FieldName.SURVEYID));
				}
				SurveyDao lvSurveyDao = new SurveyDao();
				lvSurveyDao.queryDocument(new JsonObject().put("$in", lst), rs -> {
					this.CompleteGenerateResponse(CodeMapping.C0000.toString(), "", rs.result());
				});
			} else {
				this.CompleteGenerateResponse(CodeMapping.C1111.toString(), "Null Data", null);
			}
		});
	}

}
