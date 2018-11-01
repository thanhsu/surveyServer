package com.survey.dbservice.dao;

import com.survey.utils.CodeMapping;
import com.survey.utils.FieldName;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

public class SurveyCategoryDao extends SurveyBaseDao {
	public static final String SurveyCategoryCollection = "survey_category";

	public SurveyCategoryDao() {
		setCollectionName(SurveyCategoryCollection);
	}

	public void retrieveCategory(String id) {
		JsonObject query = new JsonObject();
		if (id != null) {
			query.put(FieldName._ID, id);
		}
		this.queryDocument(query, handler -> {
			this.CompleteGenerateResponse(CodeMapping.C0000.toString(), "", handler.result());
		});
	}


}
