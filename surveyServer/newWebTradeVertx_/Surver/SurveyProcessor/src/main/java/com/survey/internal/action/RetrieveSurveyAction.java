package com.survey.internal.action;

import java.util.HashMap;
import java.util.List;

import com.survey.constant.SurveyRequestName;
import com.survey.dbservice.dao.FavouriteSurveyDao;
import com.survey.dbservice.dao.SurveyDao;
import com.survey.dbservice.dao.SurveyPushlishDao;
import com.survey.dbservice.dao.SurveySubmitDao;
import com.survey.utils.CodeMapping;
import com.survey.utils.FieldName;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class RetrieveSurveyAction extends InternalSurveyBaseAction {

	@Override
	public void doProccess() {
		String actionRequest = getMessageBody().getString(FieldName.METHOD);
		switch (actionRequest) {
		// getAllPublic Servey
		case SurveyRequestName.RETRIEVE_PUBLIC:
			this.retrievepublic(getMessageBody());
			break;
		case SurveyRequestName.RETRIEVE_MYSURVEY:
			this.retrievemysurvey(getMessageBody());
			break;
		case SurveyRequestName.RETRIEVE_SEARCH:
			this.searchSurvey(getMessageBody());
			break;
		case SurveyRequestName.RETRIEVE_SUBMIT:
			this.retrieveSubmittedSurvey(getMessageBody());
			break;

		case SurveyRequestName.RETRIEVE_FAVOURITE:
			this.retrieveFavourite(getMessageBody());
			break;

		case "test": {
			this.test(getMessageBody());
			break;
		}

		case SurveyRequestName.DETAIL:
			this.retrieveDetailSurvey(getMessageBody());
			break;

		case SurveyRequestName.CREATE:
			this.createSurvey(getMessageBody());
			break;
		case SurveyRequestName.UPDATE:
			this.updateSurvey(getMessageBody());
			break;

		case SurveyRequestName.RETRIEVE_CLOSE:
			this.retrieveClose(getMessageBody());
			break;

		default:
			break;
		}
	}

	public void retrievepublic(JsonObject request) {
		JsonObject rs = new JsonObject();
		rs.put(FieldName.STATUS, "N").put(FieldName.STATE, "A").put(FieldName.ISTEMP, false);
		SurveyDao lvDao = new SurveyDao();
		Future<JsonObject> lvResult = Future.future();
		Future<Long> count = Future.future();
		CompositeFuture lvComp = CompositeFuture.all(lvResult, count);
		lvComp.setHandler(handler -> {
			// lvResult.result().getJsonObject("data").put("total", count.result());
			response.complete(lvResult.result().put("total", count.result()));
		});
		lvDao.retriveCountTotalResponseData(rs).setHandler(handler -> {
			count.complete(handler.result());
		});
		lvDao.retriveAndCountTotalResponseData(request.getString(FieldName.USERNAME), rs,
				/* new JsonObject().put(FieldName.QUESTIONDATA, 0) */null);
		lvDao.getMvFutureResponse().setHandler(handler -> {
			lvResult.complete(handler.result());
		});
	}

	public void retrievemysurvey(JsonObject request) {
		Future<JsonObject> lvResult = Future.future();
		Future<Long> count = Future.future();
		CompositeFuture lvComp = CompositeFuture.all(lvResult, count);
		JsonObject qr = new JsonObject().put(FieldName.USERNAME, request.getString(FieldName.USERNAME));
		if (request.containsKey(FieldName.STATUS)) {
			qr.put(FieldName.STATUS, request.getString(FieldName.STATUS));
		}

		lvComp.setHandler(handler -> {
			// lvResult.result().getJsonObject("data").put("total", count.result());
			response.complete(lvResult.result().put("total", count.result()));
		});

		SurveyDao lvDao = new SurveyDao();
		lvDao.retriveCountTotalResponseData(qr).setHandler(handler -> {
			count.complete(handler.result());
		});
		lvDao.retriveAndCountTotalResponseData(request.getString(FieldName.USERNAME), qr,
				/* new JsonObject().put(FieldName.QUESTIONDATA, 0) */null);
		lvDao.getMvFutureResponse().setHandler(handler -> {
			lvResult.complete(handler.result());

		});
		/*
		 * RetrieveMySurvey lvRetrieveMySurvey = new RetrieveMySurvey();
		 * lvRetrieveMySurvey.doProcess(request);
		 * lvRetrieveMySurvey.getMvResponse().setHandler(rs -> { if (rs.succeeded()) {
		 * response.complete(rs.result()); } else {
		 * response.fail(rs.cause().getMessage()); } });
		 */
	}

	public void retrieveSubmittedSurvey(JsonObject request) {
		JsonObject rs = new JsonObject();
		rs.put(FieldName.USERNAME, request.getString(FieldName.USERNAME));
		rs.put(FieldName.ACTION, "retrievesubmitsurvey");
		SurveySubmitDao lvSurveySubmitDao = new SurveySubmitDao();
		lvSurveySubmitDao.getAllServeyDone(request.getString(FieldName.USERNAME));
		response = lvSurveySubmitDao.getMvFutureResponse();
	}

	public void retrieveDetailSurvey(JsonObject request) {
		JsonObject rs = new JsonObject();
		rs.put(FieldName.USERNAME, request.getString(FieldName.USERNAME));
		rs.put(FieldName._ID, request.getString(FieldName.SURVEYID));
		SurveyDao lvSurveyDao = new SurveyDao();
		lvSurveyDao.retriveAndCountTotalResponseData(
				request.getString(FieldName.USERNAME) == null ? "" : request.getString(FieldName.USERNAME), rs, null);
		lvSurveyDao.getMvFutureResponse().setHandler(handler -> {
			response.complete(handler.result());
		});
	}

	public void searchSurvey(JsonObject reqeuest) {
		int record = reqeuest.getInteger(FieldName.RECORD) == null ? 0 : reqeuest.getInteger(FieldName.RECORD);
		long lastsurveyinputtime = reqeuest.getLong(FieldName.INPUTTIME) == null ? 0
				: reqeuest.getLong(FieldName.INPUTTIME);
		JsonObject searchValue = reqeuest.getJsonObject(FieldName.SEARCH) == null ? new JsonObject()
				: reqeuest.getJsonObject(FieldName.SEARCH);
		SurveyDao lvSurveyDao = new SurveyDao();
		String title = searchValue.getString(FieldName.TITLE);
		JsonArray category = searchValue.getJsonArray(FieldName.LISTCATEGORYID);
		JsonObject point = searchValue.getJsonObject(FieldName.POINT);
		JsonObject createdate = searchValue.getJsonObject(FieldName.PUSHLISHDATE);
		JsonObject query = new JsonObject();
		if (lastsurveyinputtime != 0) {
			query.put(FieldName.INPUTTIME, new JsonObject().put("$lt", lastsurveyinputtime));
		}
		if (title != null) {
			query.put(FieldName.TITLE, new JsonObject().put("$regex", title));
		}
		if (category != null) {
			query.put(FieldName.LISTCATEGORYID, category);
		}

		if (searchValue.containsKey(FieldName.ISTEMP)) {
			query.put(FieldName.ISTEMP, searchValue.getBoolean(FieldName.ISTEMP));
		} else {
			query.put(FieldName.ISTEMP, false);
		}
		if (createdate != null) {
			query.put(FieldName.PUSHLISHDATE, createdate);
		}
		Future<JsonArray> listData = Future.future();

		Future<JsonArray> listData2 = Future.future();

		CompositeFuture lvAllData = CompositeFuture.all(listData, listData2);

		lvAllData.setHandler(handler -> {
			if (handler.succeeded()) {
				HashMap<String, JsonObject> data = new HashMap<>();
				for (int i = 0; i < listData.result().size(); i++) {
					data.put(listData.result().getJsonObject(i).getString(FieldName._ID),
							listData.result().getJsonObject(i));
				}

				for (int i = 0; i < listData2.result().size(); i++) {
					if (!data.containsKey(listData2.result().getJsonObject(i).getString(FieldName._ID))) {
						listData.result().add(listData2.result().getJsonObject(i));
					}
				}
				// listData.result().getList().subList(0, record);
				if (record != 0) {
					JsonObject responseDT = new JsonObject();
					responseDT.put(FieldName.CODE, CodeMapping.C0000.toString());
					responseDT.put(FieldName.MESSAGE, "");
					responseDT.put("PageRemain", (listData.result().size() / record));
					if (record >= listData.result().size()) {
						responseDT.put(FieldName.DATA, listData.result());
					} else {
						responseDT.put(FieldName.DATA, listData.result().getList().subList(0, record));
					}
					response.complete(responseDT);
					/*
					 * this.CompleteGenerateResponse(CodeMapping.C0000.toString(), "",
					 * listData.result().getList().subList(0, record), response);
					 */
				} else {
					this.CompleteGenerateResponse(CodeMapping.C0000.toString(), "", listData.result(), response);
				}
			} else {
				this.CompleteGenerateResponse(CodeMapping.C0000.toString(), "", new JsonArray(), response);
			}
		});
		if (query.isEmpty()) {
			listData.complete(new JsonArray());
		} else {
			lvSurveyDao.retriveAndCountTotalResponseData(
					reqeuest.getString(FieldName.USERNAME) == null ? "" : reqeuest.getString(FieldName.USERNAME), query,
					/* new JsonObject().put(FieldName.QUESTIONDATA, 0) */null);
			lvSurveyDao.getMvFutureResponse().setHandler(handler -> {
				listData.complete(handler.result().getJsonArray(FieldName.DATA));
			});
		}

		if (point != null) {
			// get list survey from pushlish collection and retrieve search
			SurveyPushlishDao lvDao = new SurveyPushlishDao();
			lvDao.retrieveSearchPushlishSuvey(point).setHandler(handler2 -> {
				if (handler2.succeeded()) {
					List<JsonObject> lvTmp = handler2.result();
					JsonArray listID = new JsonArray();
					for (int i = 0; i < lvTmp.size(); i++) {
						listID.add(lvTmp.get(i).getString(FieldName.SURVEYID));
					}
					if (listID.isEmpty()) {
						listData2.complete(new JsonArray());
					} else {
						SurveyDao lvSurveyDao2 = new SurveyDao();
						JsonObject query2 = new JsonObject().put(FieldName._ID, new JsonObject().put("$in", listID));
						if (lastsurveyinputtime != 0) {
							query2.put(FieldName.INPUTTIME, new JsonObject().put("$lt", lastsurveyinputtime));
						}
						lvSurveyDao2.retriveAndCountTotalResponseData(
								reqeuest.getString(FieldName.USERNAME) == null ? ""
										: reqeuest.getString(FieldName.USERNAME),
								query2, /* new JsonObject().put(FieldName.QUESTIONDATA, 0) */null);
						lvSurveyDao2.getMvFutureResponse().setHandler(handler -> {
							listData2.complete(handler.result().getJsonArray(FieldName.DATA));
						});
					}
				} else {
					listData2.complete(new JsonArray());
				}
			});
		} else {
			listData2.complete(new JsonArray());
		}
	}

	public void createSurvey(JsonObject data) {
		/*
		 * SurveyDao lvSurveyDao = new SurveyDao();
		 * lvSurveyDao.createSurvey(data.getString(FieldName.USERNAME),
		 * data.getString(FieldName.TITLE),
		 * data.getString(FieldName.CATEGORYID)).setHandler(res -> { JsonObject lvTmp =
		 * new JsonObject(); if (res.succeeded()) { lvTmp.put(FieldName.CODE,
		 * CodeMapping.C0000.toString()).put(FieldName.DATA, new
		 * JsonObject().put(FieldName.SURVEYID, res.result())); } else {
		 * lvTmp.put(FieldName.CODE,
		 * CodeMapping.C1111.toString()).put(FieldName.MESSAGE,
		 * res.cause().getMessage()); } response.complete(lvTmp); });
		 */
	}

	private void retrieveFavourite(JsonObject body) {
		FavouriteSurveyDao lvDao = new FavouriteSurveyDao();
		String username = body.getString(FieldName.USERNAME);
		lvDao.retrieveAllFavourite(username);
		lvDao.getMvFutureResponse().setHandler(handler -> {
			response.complete(handler.result());
		});
	}

	public void updateSurvey(JsonObject data) {
		/*
		 * String lvSurveyID = data.getString(FieldName.SURVEYID); JsonObject setting =
		 * data.getJsonObject(FieldName.SETTING); JsonArray question =
		 * data.getJsonArray(FieldName.QUESTIONDATA); JsonObject themeData =
		 * data.getJsonObject(FieldName.THEME); JsonObject rule =
		 * data.getJsonObject(FieldName.RULEDATA);
		 * 
		 * SurveyDao lvSurveyDao = new SurveyDao(); lvSurveyDao.UpdateSurvey(lvSurveyID,
		 * question, setting, rule, themeData); response =
		 * lvSurveyDao.getMvFutureResponse();
		 */
	}

	private void retrieveClose(JsonObject data) {
		SurveyDao lvSurveyDao2 = new SurveyDao();
		Future<JsonArray> handler = Future.future();
		lvSurveyDao2.queryDocumentRunCmd(
				new JsonObject().put(FieldName.USERNAME, data.getString(FieldName.USERNAME)).put(FieldName.STATE, "C"),
				new JsonObject().put(FieldName.QUESTIONDATA, 0), new JsonObject().put(FieldName.INPUTTIME, -1),
				handler);
		lvSurveyDao2.getMvFutureResponse().setHandler(handler3 -> {
			response.complete(handler3.result());
		});
		/*
		 * lvSurveyDao2.queryDocument( new JsonObject().put(FieldName.USERNAME,
		 * data.getString(FieldName.USERNAME)).put(FieldName.STATE, "C"), handler2 -> {
		 * if (handler.succeeded()) {
		 * this.CompleteGenerateResponse(CodeMapping.C0000.toString(), "",
		 * handler2.result(), response); } else {
		 * this.CompleteGenerateResponse(CodeMapping.C1111.toString(),
		 * handler2.cause().getMessage(), null, response); } });
		 */
	}

	public void test(JsonObject request) {
		SurveyDao lvDao = new SurveyDao();
		lvDao.retriveAndCountTotalResponseData(request.getString(FieldName.USERNAME),
				new JsonObject().put(FieldName.USERNAME, request.getString(FieldName.USERNAME)),
				/* new JsonObject().put(FieldName.QUESTIONDATA, 0) */null);
		lvDao.getMvFutureResponse().setHandler(handler -> {
			response.complete(handler.result());

		});
	}

}
