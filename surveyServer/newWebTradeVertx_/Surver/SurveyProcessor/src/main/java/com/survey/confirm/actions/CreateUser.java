package com.survey.confirm.actions;

import com.survey.utils.FieldName;
import com.survey.utils.SurveyMailCenter;

import io.vertx.core.json.JsonObject;

public class CreateUser extends BaseConfirmAction {

	@Override
	public void doProcess(JsonObject msg) {
		String username = msg.getString(FieldName.USERNAME);
		String success = String.valueOf(msg.getValue(FieldName.SUCCESS));
		if(success.equals("1")) {
			
		}else {
			//SurveyMailCenter.getInstance().sendEmail(to, subject, cc, messageData)
		}
	}

}