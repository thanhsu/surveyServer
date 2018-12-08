package com.survey.dbservice.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.survey.utils.CodeMapping;
import com.survey.utils.FieldName;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.UpdateOptions;

public class CardDao extends SurveyBaseDao {
	public static final String collectionCardCategory = "cardcategory";
	public static final String collectionCardData = "carddata";

	public void getListCardCateGory(String state, Handler<AsyncResult<List<JsonObject>>> handler) {
		this.setCollectionName(collectionCardCategory);
		JsonObject qr = new JsonObject();
		if (!state.equals("")) {
			qr.put(FieldName.STATE, state);
		}
		this.queryDocument(qr, handler);
	}

	public void addNewCategory(String categoryID, String categoryName, String imageLink, JsonArray lstValue) {
		this.setCollectionName(collectionCardCategory);
		JsonObject js = new JsonObject();
		this.queryDocument(new JsonObject().put(FieldName.CATEGORYID, categoryID).put(FieldName.STATE, "A"),
				handler -> {
					if (handler.result() != null) {
						if (!handler.result().isEmpty()) {
							js.put(FieldName.CATEGORYID, categoryID).put(FieldName.CATEGORY, categoryName)
									.put(FieldName.IMAGE, imageLink).put(FieldName.STATE, "A");
							js.put(FieldName._ID, handler.result().get(0).getString(FieldName._ID));
							js.put(FieldName.LISTVALUE, lstValue);
							this.saveDocument(js);
							return;
						}
					}
					js.put(FieldName.CATEGORYID, categoryID).put(FieldName.CATEGORY, categoryName)
							.put(FieldName.IMAGE, imageLink).put(FieldName.STATE, "A");
					this.saveDocument(js);
				});

	}

	public void disableCardCategory(String categoryID) {
		this.setCollectionName(collectionCardCategory);
		this.updateDocument(new JsonObject().put(FieldName.CATEGORYID, categoryID),
				new JsonObject().put(FieldName.STATE, "D"), new UpdateOptions(false), handler -> {
					this.CompleteGenerateResponse(CodeMapping.C0000.name(), CodeMapping.C0000.value(), null);
				});
	}

	public io.vertx.core.Future<JsonObject> retrieveCard(String username, String categoryID, String value) {
		io.vertx.core.Future<JsonObject> handler = Future.future();
		this.setCollectionName(collectionCardData);
		this.queryDocument(new JsonObject().put(FieldName.CATEGORYID, categoryID).put(FieldName.VALUE, value)
				.put(FieldName.STATE, "A"), h -> {
					if (h.succeeded() && h.result() != null) {
						if (!h.result().isEmpty()) {
							JsonObject cardData = h.result().get(0);
							handler.complete(cardData);
							doneThisCard(cardData.getString(FieldName._ID), username);
							return;
						}
					}
					handler.complete(null);
				});
		return handler;
	}

	public void newCardData(String categoryID, String value, String amount, String series, String code) {
		this.setCollectionName(collectionCardData);
		JsonObject lvJsonObject = new JsonObject().put(FieldName.CATEGORYID, categoryID).put(FieldName.VALUE, value)
				.put(FieldName.AMOUNT, amount).put(FieldName.SERIESID, series).put(FieldName.CODE, code)
				.put(FieldName.INPUTTIME, new Date().getTime()).put(FieldName.STATE, "A");

		this.saveDocument(lvJsonObject, handler -> {
			if (handler.succeeded()) {
				this.CompleteGenerateResponse(CodeMapping.C0000.name(), "Success",
						new JsonObject().put(FieldName.SUCCESS, true));
			} else {
				this.CompleteGenerateResponse(CodeMapping.C1111.name(), "Fail",
						new JsonObject().put(FieldName.SUCCESS, false));
			}
		});
	}

	public Future<JsonObject> retrieveCardDetail(String cardID) {
		Future<JsonObject> lvFuture = Future.future();
		this.setCollectionName(collectionCardData);
		JsonObject command = new JsonObject();
		JsonArray pipeline = new JsonArray();
		pipeline.add(new JsonObject().put("$match", new JsonObject().put(FieldName._ID, cardID)));
		pipeline.add(new JsonObject().put("$lookup",
				new JsonObject().put("from", collectionCardCategory).put("localField", FieldName.CATEGORYID)
						.put("foreignField", FieldName.CATEGORYID).put("as", "cardcategory")));

		command.put("aggregate", collectionCardData);
		command.put("cursor", new JsonObject().put("batchSize", 1000));
		command.put("pipeline", pipeline);

		BaseDaoConnection.getInstance().getMongoClient().runCommand("aggregate", command, resultHandler -> {
			if (resultHandler.succeeded()) {
				JsonArray result = resultHandler.result().getJsonObject("cursor").getJsonArray("firstBatch");
				if (result != null) {
					lvFuture.complete(result.getJsonObject(0));
					return;
				}
			}
			lvFuture.complete(null);

		});

		return lvFuture;
	}
	
	public Future<JsonArray> retrieveCardDetail(JsonObject qr) {
		Future<JsonArray> lvFuture = Future.future();
		this.setCollectionName(collectionCardData);
		JsonObject command = new JsonObject();
		JsonArray pipeline = new JsonArray();
		pipeline.add(new JsonObject().put("$match", qr));
		pipeline.add(new JsonObject().put("$lookup",
				new JsonObject().put("from", collectionCardCategory).put("localField", FieldName.CATEGORYID)
						.put("foreignField", FieldName.CATEGORYID).put("as", "cardcategory")));

		command.put("aggregate", collectionCardData);
		command.put("cursor", new JsonObject().put("batchSize", 1000));
		command.put("pipeline", pipeline);

		BaseDaoConnection.getInstance().getMongoClient().runCommand("aggregate", command, resultHandler -> {
			if (resultHandler.succeeded()) {
				JsonArray result = resultHandler.result().getJsonObject("cursor").getJsonArray("firstBatch");
				if (result != null) {
					lvFuture.complete(result);
					return;
				}
			}
			lvFuture.complete(null);

		});

		return lvFuture;
	}

	public void retrieveAllCardAvailable(String categoryID, Handler<AsyncResult<List<JsonObject>>> handler) {
		this.setCollectionName(collectionCardData);
		this.queryDocument(new JsonObject().put(FieldName.STATE, "A"), handler);
	}

	public void doneThisCard(String id, String username) {
		this.setCollectionName(collectionCardData);
		this.updateDocument(new JsonObject().put(FieldName._ID, id),
				new JsonObject().put(FieldName.STATE, "D").put(FieldName.USERNAME, username), new UpdateOptions(false), handler -> {
				});
	}

	public void revertThisCard(String id) {
		this.updateDocument(new JsonObject().put(FieldName._ID, id).put(FieldName.USERNAME, ""),
				new JsonObject().put(FieldName.STATE, "A"), new UpdateOptions(false), handler -> {
				});
	}
}
