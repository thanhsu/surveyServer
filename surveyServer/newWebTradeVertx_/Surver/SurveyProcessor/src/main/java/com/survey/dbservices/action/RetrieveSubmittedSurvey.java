package com.survey.dbservices.action;

import com.survey.utils.FieldName;

import io.vertx.core.json.JsonObject;

public class RetrieveSubmittedSurvey extends BaseAdminServiceAction {

	@Override
	public void doProcess(JsonObject body) {
		String username =  body.getString(FieldName.USERNAME);
		
		
	}
	
}
