package com.survey.confirm.actions;

import com.survey.utils.FieldName;

import io.vertx.core.json.JsonObject;

public class StopSurvey extends BaseConfirmAction {

	@Override
	public void doProcess(JsonObject msg) {
		String surveyID = msg.getString(FieldName.SURVEYID);
		boolean success = msg.getBoolean(FieldName.SUCCESS);
		String transID = msg.getString(FieldName.TRANID);
		String userBalance = msg.getString(FieldName.USERBALANCE);
		
		String username =  msg.getString(FieldName.USERNAME);
		
		
	}

}
