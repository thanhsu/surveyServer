package com.survey.dbservice.dao;

import java.util.List;

import com.survey.utils.FieldName;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.UpdateOptions;

public class CardDao extends SurveyBaseDao {
	public static final String collectionCardCategory = "cardcategory";
	public static final String collectionCardData = "carddata";

	public void getListCardCateGory(Handler<AsyncResult<List<JsonObject>>> handler) {
		this.setCollectionName(collectionCardCategory);
		this.queryDocument(new JsonObject().put(FieldName.STATE, "A"), handler);
	}

	public void addNewCategory(String categoryID, String categoryName, String imageLink) {
		this.setCollectionName(collectionCardCategory);
		JsonObject js = new JsonObject();
		js.put(FieldName.CATEGORYID, categoryID).put(FieldName.CATEGORY, categoryName).put(FieldName.IMAGE, imageLink);
		this.saveDocument(js);
		this.setMvResponse(new JsonObject());
	}

	public void retrieveCard(String username, String categoryID, String value,
			io.vertx.core.Future<JsonObject> handler) {
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
					handler.complete(new JsonObject());
				});

	}

	public void doneThisCard(String id, String username) {
		this.updateDocument(new JsonObject().put(FieldName._ID, id).put(FieldName.USERNAME, username),
				new JsonObject().put(FieldName.STATE, "A"), new UpdateOptions(false), handler -> {
				});
	}
}
