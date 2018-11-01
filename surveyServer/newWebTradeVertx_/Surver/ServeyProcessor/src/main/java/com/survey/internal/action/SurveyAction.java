package com.survey.internal.action;

import com.survey.constant.EventBusDiscoveryConst;
import com.survey.constant.SurveyRequestName;
import com.survey.utils.FieldName;
import com.survey.utils.VertxServiceCenter;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.Record;

public class SurveyAction extends BaseSurveyInternalAction {

	@Override
	public void doProccess() {
		String actionRequest = getMessageBody().getString("action");
		switch (actionRequest) {
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

			break;
		default:
			break;
		}
	}

	public void retrievepublic(JsonObject request) {
		JsonObject rs = new JsonObject();
		rs.put(FieldName.ENABLE, "N").put(FieldName.STATE, "A");
		searchSurvey(rs);
	}

	public void retrievemysurvey(JsonObject request) {
		VertxServiceCenter.getInstance().sendNewMessage(EventBusDiscoveryConst.SURVEYDBDISCOVERY.toString(),
				new JsonObject().put(FieldName.ACTION, "retrievemysurvey").put(FieldName.USERNAME,
						request.getString(FieldName.USERNAME)),
				response);
	}

	public void retrieveSubmittedSurvey(JsonObject request) {
		JsonObject rs = new JsonObject();
		rs.put(FieldName.USERNAME, request.getString(FieldName.USERNAME));
		rs.put(FieldName.ACTION, "retrievesubmitsurvey");
		VertxServiceCenter.getInstance().sendNewMessage(EventBusDiscoveryConst.SURVEYDBDISCOVERY.toString(),
				rs, response);
	}

	public void searchSurvey(JsonObject reqeuest) {
		JsonObject rs = new JsonObject();
		rs.put(FieldName.ACTION, "retrievesurvey");
		rs.put(FieldName.SEARCH, reqeuest);
		VertxServiceCenter.getInstance().getDiscovery().getRecord(
				new JsonObject().put("name", EventBusDiscoveryConst.SURVEYDBDISCOVERY.toString()),
				new Handler<AsyncResult<Record>>() {
					@Override
					public void handle(AsyncResult<Record> event) {
						if (event.succeeded() && event.result() != null) {
							Record record = event.result();
							VertxServiceCenter.getEventbus()
									.<JsonObject>send(record.getLocation().getString("endpoint"), rs, rs -> {
										if (rs.succeeded()) {
											response.complete(rs.result().body());
										} else {
											response.fail(rs.cause().getMessage());
										}
									});
						} else {
							response.fail(event.cause());
							response.completer();
						}
					}
				});
	}

}
