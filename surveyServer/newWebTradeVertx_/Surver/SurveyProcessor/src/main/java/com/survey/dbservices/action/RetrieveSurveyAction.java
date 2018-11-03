package com.survey.dbservices.action;

import com.survey.dbservice.dao.SurveyDao;
import com.survey.utils.CodeMapping;
import com.survey.utils.FieldName;

import io.vertx.core.json.JsonObject;

public class RetrieveSurveyAction extends BaseDbServiceAction {

	@Override
	public void doProcess(JsonObject body) {
		JsonObject searchValue = body.getJsonObject(FieldName.SEARCH);
		SurveyDao lvSurveyDao = new SurveyDao();
		lvSurveyDao.retrieveSurvey(searchValue, h -> {
			JsonObject lvTmp = new JsonObject();
			if (h.succeeded()) {
				lvTmp.put(FieldName.CODE, CodeMapping.C0000.toString()).put(FieldName.DATA, h.result());
			} else {
				lvTmp.put(FieldName.CODE, CodeMapping.C1111.toString()).put(FieldName.MESSAGE, h.cause().getMessage());
			}
			mvResponse.complete(lvTmp);
		});
	}

}
