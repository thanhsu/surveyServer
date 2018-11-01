package com.survey.dbservices.action;

import com.survey.dbservice.dao.SurveyDao;
import com.survey.utils.FieldName;

import io.vertx.core.json.JsonObject;

public class UpdateSurveyDataAction extends BaseDbServiceAction {

	@Override
	public void doProcess(JsonObject body) {
		String surveyID = body.getString(FieldName._ID);
		JsonObject updateData = body.getJsonObject(FieldName.UPDATEDATA);

		SurveyDao lvSurveyDao = new SurveyDao();
		lvSurveyDao.updateSurveyData(surveyID, updateData);
	}

}
