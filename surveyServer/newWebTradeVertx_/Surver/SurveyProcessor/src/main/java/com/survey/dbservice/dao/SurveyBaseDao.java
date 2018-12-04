package com.survey.dbservice.dao;

import java.util.List;

import com.mongodb.client.model.Aggregates;
import com.survey.utils.CodeMapping;
import com.survey.utils.FieldName;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClientDeleteResult;
import io.vertx.ext.mongo.UpdateOptions;

public abstract class SurveyBaseDao {

	protected Future<JsonObject> mvFutureResponse = Future.future();

	private JsonObject mvResponse = new JsonObject();
	private String CollectionName = "";

	public void CompleteGenerateResponse(String code, String message, Object data) {
		mvResponse = new JsonObject();
		mvResponse.put(FieldName.CODE, code);
		mvResponse.put(FieldName.MESSAGE, message);
		mvResponse.put(FieldName.DATA, data);
		mvFutureResponse.complete(mvResponse);
		mvFutureResponse.completer();
	}

	public void CompleteGenerateResponse(String code, String message, JsonObject data, Future<JsonObject> result) {
		mvResponse = new JsonObject();
		mvResponse.put(FieldName.CODE, code);
		mvResponse.put(FieldName.MESSAGE, message);
		mvResponse.put(FieldName.DATA, data);
		result.complete(mvResponse);
		result.completer();
	}

	public void queryDocument(JsonObject query, Handler<AsyncResult<List<JsonObject>>> handler) {
		BaseDaoConnection.getInstance().getMongoClient().find(CollectionName, query, handler);
	}

	public void queryDocumentRunCmd(JsonObject query, JsonObject project, JsonObject sort, Future<JsonArray> handler) {
		JsonObject command = new JsonObject();
		JsonArray pipeline = new JsonArray();
		pipeline.add(new JsonObject().put("$match", query));
		if (project != null) {
			pipeline.add(new JsonObject().put("$project", project));
		}
		if (!sort.isEmpty()) {
			pipeline.add(new JsonObject().put("$sort", sort));
		}
		command.put("aggregate", this.getCollectionName());
		command.put("cursor", new JsonObject().put("batchSize", 1000));
		command.put("pipeline", pipeline);

		BaseDaoConnection.getInstance().getMongoClient().runCommand("aggregate", command, resultHandler -> {
			if (resultHandler.succeeded()) {
				JsonArray result = resultHandler.result().getJsonObject("cursor").getJsonArray("firstBatch");
				if (result != null) {

					this.CompleteGenerateResponse(CodeMapping.C0000.toString(), CodeMapping.C0000.value(), result);
				} else {
					this.CompleteGenerateResponse(CodeMapping.C1111.toString(), CodeMapping.C1111.value(), null);

				}
				handler.complete(result);
			} else {
				this.CompleteGenerateResponse(CodeMapping.C1111.toString(), CodeMapping.C1111.value(),
						resultHandler.cause().getMessage());
				handler.fail(resultHandler.cause().getMessage());
			}

		});

	}

	public void delteDocument(JsonObject query, Handler<AsyncResult<MongoClientDeleteResult>> handler) {
		BaseDaoConnection.getInstance().getMongoClient().removeDocument(CollectionName, query, handler);
	}

	// public void runCommand() {
	// JsonArray lvJsonArr= new JsonArray();
	// JsonObject command = new JsonObject()
	// .put("aggregate", this.CollectionName)
	// .put("pipeline",lvJsonArr);
	// BaseDaoConnection.getInstance().getMongoClient().
	// runCommand(commandName, command, resultHandler)
	//
	// }

	public Future<JsonObject> findOneByID(String id) {
		mvFutureResponse = Future.future();
		BaseDaoConnection.getInstance().getMongoClient().findOne(CollectionName,
				new JsonObject().put(FieldName._ID, id), null, resultHandler -> {
					if (resultHandler.succeeded() && resultHandler.result() != null) {
						mvFutureResponse.complete(resultHandler.result());
					} else {
						mvFutureResponse.fail("null");
					}
				});
		return mvFutureResponse;
	}

	public void saveDocument(JsonObject data, Handler<AsyncResult<String>> handler) {
		BaseDaoConnection.getInstance().getMongoClient().save(CollectionName, data, handler);
	}

	public Future<String> saveDocumentReturnID(JsonObject data) {
		Future<String> result = Future.future();
		BaseDaoConnection.getInstance().getMongoClient().save(CollectionName, data, handler -> {
			if (handler.succeeded()) {
				result.complete(handler.result());
			} else {
				result.fail(handler.cause());
			}
		});
		return result;
	}

	public void saveDocumentReturnID(JsonObject data, Future<String> result) {
		BaseDaoConnection.getInstance().getMongoClient().save(CollectionName, data, handler -> {
			if (handler.succeeded()) {
				result.complete(handler.result());
			} else {
				result.fail(handler.cause());
			}
		});
	}
	
	public void saveDocument(JsonObject data) {
		BaseDaoConnection.getInstance().getMongoClient().save(CollectionName, data, handler -> {
		});
	}

	public void updateDocument(JsonObject query, JsonObject data, UpdateOptions option,
			Handler<AsyncResult<Void>> handler) {
		JsonObject lvupdateData = new JsonObject().put("$set", data);
		BaseDaoConnection.getInstance().getMongoClient().updateWithOptions(getCollectionName(), query, lvupdateData,
				option == null ? new UpdateOptions().setUpsert(false) : option, handler);
	}

	public JsonObject getMvResponse() {
		return mvResponse;
	}

	public void setMvResponse(JsonObject mvResponse) {
		this.mvResponse = mvResponse;
	}

	public String getCollectionName() {
		return CollectionName;
	}

	public void setCollectionName(String collectionName) {
		CollectionName = collectionName;
	}

	public Future<JsonObject> getMvFutureResponse() {
		return mvFutureResponse;
	}

}
