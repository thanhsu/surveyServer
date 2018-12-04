package com.survey.dbservice.dao;

import java.util.List;

import com.survey.utils.FieldName;

import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.FindOptions;

public class SurveyPushlishDao extends SurveyBaseDao {
	public static final String collectionname = "surveypushlish";

	public SurveyPushlishDao() {
		setCollectionName(collectionname);
	}

	public Future<String> newPushlishAction(String surveyID, double limitResp, double pointPerOne, double initialFund,
			boolean noti, double limitFund) {
		return this.saveDocumentReturnID(new JsonObject().put(FieldName.SURVEYID, surveyID)
				.put(FieldName.INITIALFUND, String.valueOf(initialFund))
				.put(FieldName.LIMITFUND, String.valueOf(limitFund))
				.put(FieldName.LIMITRESPONSE, String.valueOf(limitResp))
				.put(FieldName.PAYOUT, String.valueOf(pointPerOne)).put(FieldName.NOTIFY, noti));

	}

	public Future<JsonObject> retrievePushlishSuvey(String id) {
		Future<JsonObject> lvResult = Future.future();
		BaseDaoConnection.getInstance().getMongoClient().findWithOptions(getCollectionName(),
				new JsonObject().put(FieldName._ID, id), new FindOptions(), resultHandler -> {
					if (resultHandler.succeeded() && resultHandler.result() != null) {
						if (resultHandler.result().isEmpty()) {
							lvResult.complete(null);
						} else {
							lvResult.complete(resultHandler.result().get(0));
						}
					} else {
						lvResult.complete(null);
					}
				});
		return lvResult;
	}

	public Future<List<JsonObject>> retrieveSearchPushlishSuvey(JsonObject searchValue) {
		Future<List<JsonObject>> lvResult = Future.future();
		BaseDaoConnection.getInstance().getMongoClient().findWithOptions(getCollectionName(), searchValue,
				new FindOptions().setFields(new JsonObject().put("surveyid", 1)), resultHandler -> {
					if (resultHandler.succeeded() && resultHandler.result() != null) {
						lvResult.complete(resultHandler.result());
					} else {
						lvResult.complete(null);
					}
				});
		return lvResult;
	}

}
