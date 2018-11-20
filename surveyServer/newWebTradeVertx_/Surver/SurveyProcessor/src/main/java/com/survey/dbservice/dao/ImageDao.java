package com.survey.dbservice.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.survey.utils.CodeMapping;
import com.survey.utils.FieldName;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class ImageDao extends SurveyBaseDao {
	public static final String ImageCollection = "image_datas";

	public ImageDao() {
		super();
		this.setCollectionName(ImageCollection);
	}

	public void storeImage(String username, JsonArray arrImage) {
		JsonArray responseData = new JsonArray();
		List<Future> lvListFutures = new ArrayList<>();
		CompositeFuture compFuture = CompositeFuture.join(lvListFutures);

		for (int i = 0; i < arrImage.size(); i++) {
			JsonObject js = arrImage.getJsonObject(i);
			this.saveDocumentReturnID(new JsonObject().put(FieldName.TITLE, js.getString(FieldName.TITLE))
					.put(FieldName.DATA, js.getString(FieldName.DATA)).put(FieldName.INPUTTIME, new Date().getTime())
					.put(FieldName.USERNAME, username)).setHandler(handler -> {
						Future<JsonObject> lvFuture = Future.future();
						lvListFutures.add(lvFuture);
						lvFuture.complete(js.put(FieldName.IMAGEID, handler.result()));
					});
		}
		compFuture.setHandler(handler -> {
			if (handler.succeeded()) {
				for (int i = 0; i < lvListFutures.size(); i++) {
					responseData.add(lvListFutures.get(i).result());
				}
				this.CompleteGenerateResponse(CodeMapping.C0000.toString(), "storedImage", responseData);
			} else {
				this.CompleteGenerateResponse(CodeMapping.C1111.toString(), handler.cause().getMessage(), null);
			}
		});
	}

}
