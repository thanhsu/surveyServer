package com.survey.dbservices.action;

import com.survey.dbservice.dao.SurveyDao;
import com.survey.utils.CodeMapping;
import com.survey.utils.FieldName;

import io.vertx.core.json.JsonObject;

public class RetrieveMySurvey extends BaseAdminServiceAction {
	@Override
	public void doProcess(JsonObject body) {
		String lvUsername = body.getString(FieldName.USERNAME);
		SurveyDao lvSurveyDao = new SurveyDao();
		lvSurveyDao.queryDocument(new JsonObject().put(FieldName.USERNAME, lvUsername), handler -> {
			if (handler.succeeded()) {
				handler.result().forEach(action -> {
					action.put(FieldName.ENABLEEDIT, true);
					action.put(FieldName.ENABLESUBMIT, false);
				});
				JsonObject lvTmp = new JsonObject();
				lvTmp.put(FieldName.CODE, CodeMapping.C0000.toString());
				lvTmp.put(FieldName.DATA, handler.result());
				mvResponse.complete(lvTmp);
			} else {
				JsonObject lvTmp = new JsonObject();
				lvTmp.put(FieldName.CODE, CodeMapping.C1111.toString());
				lvTmp.put(FieldName.MESSAGE, handler.cause().getMessage());
				mvResponse.complete(lvTmp);
			}
		});
	}

}
