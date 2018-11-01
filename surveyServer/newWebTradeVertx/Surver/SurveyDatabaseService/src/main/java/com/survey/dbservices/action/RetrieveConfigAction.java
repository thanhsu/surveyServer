package com.survey.dbservices.action;

import com.squareup.okhttp.internal.framed.FrameReader.Handler;
import com.survey.dbservice.dao.UtilsDao;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

public class RetrieveConfigAction extends BaseDbServiceAction {

	@Override
	public void doProcess(JsonObject body) {
		UtilsDao lvUtils = new UtilsDao();
		Future<JsonObject> lvRules = lvUtils.retrieveAllRules();
		lvUtils = new UtilsDao();
		Future<JsonObject> lvCategory = lvUtils.retrieveAllCategory(new JsonObject());

		lvRules.setHandler(handler -> {
			JsonObject data = handler.result();
			mvResponse.map(mvResponse.result().put("rules", data));
		});
		lvCategory.setHandler(handler -> {
			JsonObject data = handler.result();
			mvResponse.map(mvResponse.result().put("category", data));
		});
	}

}
