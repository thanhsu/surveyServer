package com.survey.internal.action;

import com.survey.dbservice.dao.SurveyDao;
import com.survey.utils.FieldName;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class UpdateSurveyDataAction extends InternalSurveyBaseAction {

	@Override
	public void doProccess() {

		String lvSurveyID = getMessageBody().getString(FieldName.SURVEYID);
		JsonObject setting = getMessageBody().getJsonObject(FieldName.SETTING);
		JsonArray question = getMessageBody().getJsonArray(FieldName.QUESTIONDATA);
		JsonObject themeData = getMessageBody().getJsonObject(FieldName.THEME);
		JsonObject rule = getMessageBody().getJsonObject(FieldName.RULE);

		SurveyDao lvSurveyDao = new SurveyDao();
		lvSurveyDao.UpdateSurvey(lvSurveyID, question, setting, rule, themeData,
				getMessageBody().getString(FieldName.TITLE),
				getMessageBody().getString(FieldName.DESCRIPTION));
		lvSurveyDao.getMvFutureResponse().setHandler(handler->{
			response.complete(handler.result());
		});
	}

}
