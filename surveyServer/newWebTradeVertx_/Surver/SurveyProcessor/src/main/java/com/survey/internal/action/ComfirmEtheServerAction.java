package com.survey.internal.action;

import com.survey.utils.FieldName;

import io.vertx.core.json.JsonObject;

public class ComfirmEtheServerAction extends InternalSurveyBaseAction {

	@Override
	public void doProccess() {
		response.complete(new JsonObject());
		String action = getMessageBody().getString(FieldName.ACTION);
		
	}

}
