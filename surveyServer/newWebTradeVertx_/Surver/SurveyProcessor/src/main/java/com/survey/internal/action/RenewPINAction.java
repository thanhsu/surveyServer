package com.survey.internal.action;

import com.survey.dbservice.dao.UserDao;
import com.survey.utils.CodeMapping;
import com.survey.utils.FieldName;
import com.survey.utils.GenPIN;
import com.survey.utils.SurveyMailCenter;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.UpdateOptions;

public class RenewPINAction extends InternalSurveyBaseAction {

	@Override
	public void doProccess() {
		String username = getMessageBody().getString(FieldName.USERNAME);
		String email = getMessageBody().getString(FieldName.EMAIL);
		UserDao lvDao = new UserDao();
		lvDao.queryDocument(new JsonObject().put(FieldName.USERNAME, username).put(FieldName.EMAIL, email), handler->{
			if(handler.result()!=null) {
				if(!handler.result().isEmpty()) {
					String newPin = GenPIN.newPin(6);
					UserDao lvDao2 = new UserDao();
					lvDao2.updateDocument(new JsonObject().put(FieldName.USERNAME,username), new JsonObject().put(FieldName.STATUS,"A").put(FieldName.PIN, newPin), new UpdateOptions(false), handler2->{});
					SurveyMailCenter.getInstance().sendEmail(email, "Go-Survey Generate New Pin",
							"Active Account", "<p>Your account changed PIN</p><p>This is your PIN code: "+newPin+"</p>");
					this.CompleteGenerateResponse(CodeMapping.C0000.name(), "Check email for get new PIN", new JsonObject().put(FieldName.SUCCESS, true), response);
					return;
				}
			}
			this.CompleteGenerateResponse(CodeMapping.C1111.name(), CodeMapping.C1111.value(), new JsonObject().put(FieldName.SUCCESS, false), response);
		});
	}
	
}
