package com.survey.dbservice.dao;

import com.survey.utils.CodeMapping;
import com.survey.utils.FieldName;

import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class FavouriteSurveyDao extends SurveyBaseDao {
	public static final String collectionFavouriteSurvey = "favouritesurvey";

	public FavouriteSurveyDao() {
		super();
		setCollectionName(collectionFavouriteSurvey);
	}

	public Future<Boolean> storeNewFavouriteSurvey(String username, String surveyID) {
		Future<Boolean> lvResult = Future.future();

		SurveyDao lvSurveyDao = new SurveyDao();
		lvSurveyDao.retrieveSurvey(new JsonObject().put(FieldName._ID, surveyID), h -> {
			if (h.succeeded() && h.result() != null) {
				JsonObject tmp = h.result().get(0);
				if (tmp.getBoolean(FieldName.ISTEMP)
						|| tmp.getJsonObject(FieldName.SETTING).getBoolean(FieldName.FAVOURITE_ENABLE)) {
					tmp.remove(FieldName.SETTING);
					tmp.remove(FieldName.QUESTIONDATA);
					this.delteDocument(
							new JsonObject().put(FieldName.USERNAME, username).put(FieldName.SURVEYID, surveyID),
							handler2 -> {
								this.saveDocument(new JsonObject().put(FieldName.USERNAME, username)
										.put(FieldName.SURVEYID, surveyID), handler -> {
											lvResult.complete(handler.succeeded());
										});
							});

				}
			} else {
				lvResult.complete(false);
			}
		});

		return lvResult;
	}

	public void removeFavouriteSurvey(String username, JsonArray lst) {
		this.delteDocument(new JsonObject().put(FieldName.USERNAME, username).put(FieldName.SURVEYID,
				new JsonObject().put("$in", lst)), handler -> {
				});
	}

	public Future<Void> retrieveAllFavourite(String username) {
		Future<Void> lvResult = Future.future();
		/*this.queryDocument(new JsonObject().put(FieldName.USERNAME, username), handler -> {
			if (handler.succeeded()) {
				this.CompleteGenerateResponse(CodeMapping.S0000.toString(), "", handler.result());
			} else {
				this.CompleteGenerateResponse(CodeMapping.S1111.toString(), handler.cause().getMessage(), null);
			}
		});*/
		
		JsonObject command = new JsonObject();
		JsonArray pipeline = new JsonArray();
		pipeline.add(new JsonObject().put("$match", new JsonObject().put(FieldName.USERNAME, username)));
	
		
		pipeline.add(new JsonObject().put("$lookup", new JsonObject().put("from", SurveyDao.SurveyCollectionName)
				.put("localField", "_id").put("foreignField", FieldName.SURVEYID).put("as", "surveydata")));
		
		command.put("aggregate", this.getCollectionName());
		command.put("cursor", new JsonObject().put("batchSize", 1000));
		command.put("pipeline", pipeline);
		BaseDaoConnection.getInstance().getMongoClient().runCommand("aggregate", command, resultHandler -> {
			if (resultHandler.succeeded()) {
				JsonArray result = resultHandler.result().getJsonObject("cursor").getJsonArray("firstBatch");
				if(result!=null) {
					for(int i =0; i < result.size();i++) {
						result.getJsonObject(i).getJsonObject("surveydata").remove(FieldName.QUESTIONDATA);
					}
				}
				this.CompleteGenerateResponse(CodeMapping.S0000.toString(), "", result);
			} else {
				this.CompleteGenerateResponse(CodeMapping.S1111.toString(), CodeMapping.S1111.value(),
						resultHandler.cause().getMessage());
			}
			lvResult.complete();

		});
		return lvResult;
	}
}
