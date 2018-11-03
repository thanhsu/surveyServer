package com.survey.dbservices.action;

import com.survey.dbservice.dao.SurveyDao;

import io.vertx.core.json.JsonObject;

public class SubmitNewResultAction extends BaseDbServiceAction {

	@Override
	public void doProcess(JsonObject body) {
		SurveyDao lvSurveyDao = new SurveyDao();
		

	}

}
