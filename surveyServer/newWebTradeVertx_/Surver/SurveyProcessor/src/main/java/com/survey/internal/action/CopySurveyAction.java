package com.survey.internal.action;

import com.survey.dbservice.dao.SurveyDao;
import com.survey.utils.CodeMapping;
import com.survey.utils.FieldName;

import io.vertx.core.json.JsonObject;

public class CopySurveyAction extends InternalSurveyBaseAction {

	@Override
	public void doProccess() {
		String tmpSurvey = getMessageBody().getString(FieldName.TEMPSURVEYID);
		String username = getMessageBody().getString(FieldName.USERNAME);
		SurveyDao lvDao = new SurveyDao();
		lvDao.copyTempSurvey(username, tmpSurvey).setHandler(handler -> {
			JsonObject lvTmp = new JsonObject();
			if (handler.succeeded()) {
				lvTmp.put(FieldName.CODE, CodeMapping.C0000.toString()).put(FieldName.DATA,
						new JsonObject().put(FieldName.SURVEYID, handler.result()));
			} else {
				lvTmp.put(FieldName.CODE, CodeMapping.C1111.toString()).put(FieldName.MESSAGE,
						handler.cause().getMessage());
			}
			response.complete(lvTmp);
		});

	}

}
