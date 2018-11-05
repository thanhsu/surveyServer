package com.survey.internal.action;

import com.survey.dbservice.dao.SurveyDao;
import com.survey.utils.CodeMapping;
import com.survey.utils.FieldName;

import io.vertx.core.json.JsonObject;

public class NewSurveyAction extends InternalSurveyBaseAction {
	@Override
	public void doProccess() {
		SurveyDao lvSurveyDao = new SurveyDao();
		lvSurveyDao.createSurvey(getMessageBody().getString(FieldName.USERNAME),
				getMessageBody().getString(FieldName.TITLE), getMessageBody().getJsonArray(FieldName.LISTCATEGORYID)
				,getMessageBody().getString(FieldName.DESCRIPTION))
				.setHandler(res -> {
					JsonObject lvTmp = new JsonObject();
					if (res.succeeded()) {
						lvTmp.put(FieldName.CODE, CodeMapping.C0000.toString()).put(FieldName.DATA,
								new JsonObject().put(FieldName.SURVEYID, res.result()));
					} else {
						lvTmp.put(FieldName.CODE, CodeMapping.C1111.toString()).put(FieldName.MESSAGE,
								res.cause().getMessage());
					}
					response.complete(lvTmp);
				});
	}

}