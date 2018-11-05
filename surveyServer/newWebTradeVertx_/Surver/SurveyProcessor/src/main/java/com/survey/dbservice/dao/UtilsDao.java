package com.survey.dbservice.dao;

import com.survey.utils.FieldName;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

public class UtilsDao extends SurveyBaseDao {
	public static final String CategoryCollectionName = "survey_category";
	public static final String RulesCollectionName = "rulesupports";
	public static final String QuestionTmpCollectionName = "question_tmp";
	public static final String CareerCollectionName = "career";
	public static final String EthereumPointValue = "ethepointvalue";
	public static final String CashMethodCollectionName = "cashmethod";

	public Future<JsonObject> retrieveAllRules() {
		BaseDaoConnection.getInstance().getMongoClient().find(RulesCollectionName, new JsonObject(), handler -> {
			if (handler.succeeded()) {
				this.CompleteGenerateResponse("0000", "", handler.result());
			} else {
				this.CompleteGenerateResponse("1111", handler.cause().getMessage(), null);
			}

		});
		return mvFutureResponse;
	}

	public void newRules(JsonObject json) {
		this.setCollectionName(RulesCollectionName);
		this.saveDocument(json);
	}

	public void disableRule(String id) {
		this.setCollectionName(RulesCollectionName);
		this.updateDocument(new JsonObject().put(FieldName.ID, id), new JsonObject().put(FieldName.STATE, "L"), null,
				handler -> {
					if (handler.succeeded()) {
						this.CompleteGenerateResponse("0000", "", handler.result());
					} else {
						this.CompleteGenerateResponse("1111", handler.cause().getMessage(), null);
					}
				});
	}

	public void enableRule(String id) {
		this.setCollectionName(RulesCollectionName);
		this.updateDocument(new JsonObject().put(FieldName.ID, id), new JsonObject().put(FieldName.STATE, "N"), null,
				handler -> {
					if (handler.succeeded()) {
						this.CompleteGenerateResponse("0000", "", handler.result());
					} else {
						this.CompleteGenerateResponse("1111", handler.cause().getMessage(), null);
					}
				});
	}

	public Future<JsonObject> retrieveAllCategory(JsonObject query) {
		this.setCollectionName(CategoryCollectionName);
		this.queryDocument(query, handler -> {
			if (handler.succeeded()) {
				this.CompleteGenerateResponse("0000", "", handler.result());
			} else {
				this.CompleteGenerateResponse("1111", handler.cause().getMessage(), null);
			}
		});
		return mvFutureResponse;
	}

	public void newCategory(JsonObject data) {
		this.setCollectionName(CategoryCollectionName);
		this.saveDocument(data);
	}

	public void disableCategory(String id) {
		this.setCollectionName(CategoryCollectionName);
		this.updateDocument(new JsonObject().put(FieldName.ID, id), new JsonObject().put(FieldName.STATE, "L"), null,
				handler -> {
					if (handler.succeeded()) {
						this.CompleteGenerateResponse("0000", "", handler.result());
					} else {
						this.CompleteGenerateResponse("1111", handler.cause().getMessage(), null);
					}
				});
	}

	public void enableCategory(String id) {
		this.setCollectionName(CategoryCollectionName);
		this.updateDocument(new JsonObject().put(FieldName.ID, id), new JsonObject().put(FieldName.STATE, "N"), null,
				handler -> {
					if (handler.succeeded()) {
						this.CompleteGenerateResponse("0000", "", handler.result());
					} else {
						this.CompleteGenerateResponse("1111", handler.cause().getMessage(), null);
					}
				});
	}

	// With Question Tmp Data
	public void retriveAllQuestionTemp() {
		this.setCollectionName(QuestionTmpCollectionName);
		this.queryDocument(new JsonObject(), handler -> {
			if (handler.succeeded()) {
				this.CompleteGenerateResponse("0000", "", handler.result());
			} else {
				this.CompleteGenerateResponse("1111", handler.cause().getMessage(), null);
			}
		});
	}

	public void newQuestionTmp(JsonObject tmpData) {
		this.setCollectionName(QuestionTmpCollectionName);
		this.saveDocument(tmpData);
	}

	// Career

	public Future<JsonObject> retrieveAllCareer() {
		this.setCollectionName(CareerCollectionName);
		this.queryDocument(new JsonObject(), handler -> {
			if (handler.succeeded()) {
				this.CompleteGenerateResponse("0000", "", handler.result());
			} else {
				this.CompleteGenerateResponse("1111", handler.cause().getMessage(), null);
			}
		});
		return mvFutureResponse;
	}

	public Future<JsonObject> retrieveEthePointValue(boolean isHistory) {
		this.setCollectionName(EthereumPointValue);
		this.queryDocument(new JsonObject().put(FieldName.ISHISTORY, isHistory), handler -> {
			if (handler.succeeded()) {
				this.CompleteGenerateResponse("0000", "", handler.result());
			} else {
				this.CompleteGenerateResponse("1111", handler.cause().getMessage(), null);
			}
		});
		return mvFutureResponse;
	}

	public Future<JsonObject> retrieveCashMethod() {
		this.setCollectionName(CashMethodCollectionName);
		this.queryDocument(new JsonObject().put(FieldName.STATUS, "N"), handler -> {
			if (handler.succeeded()) {
				this.CompleteGenerateResponse("0000", "", handler.result());
			} else {
				this.CompleteGenerateResponse("1111", handler.cause().getMessage(), null);
			}
		});
		return mvFutureResponse;
	}

	public Future<JsonObject> retrieveCashMethodDetail(String method) {
		Future<JsonObject> lvResp = Future.future();
		this.setCollectionName(CashMethodCollectionName);
		this.queryDocument(new JsonObject().put(FieldName.METHOD, method).put(FieldName.STATUS, "N"), handler -> {
			if(handler.succeeded()&&handler.result()!=null) {
				if(handler.result().size()>0) {
					lvResp.complete(handler.result().get(0));
				}else {
					lvResp.fail("Not found");
				}
			}else {
				lvResp.fail("Not found");
			}
		});

		return lvResp;
	}
}