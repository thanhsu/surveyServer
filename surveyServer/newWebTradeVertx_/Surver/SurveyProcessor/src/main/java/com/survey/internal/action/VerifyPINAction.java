package com.survey.internal.action;

import com.survey.dbservice.dao.UserDao;
import com.survey.utils.CodeMapping;
import com.survey.utils.FieldName;

import io.vertx.core.json.JsonObject;

public class VerifyPINAction extends InternalSurveyBaseAction {

	@Override
	public void doProccess() {
		String username = getMessageBody().getString(FieldName.USERNAME);
		String pin = getMessageBody().getString(FieldName.PIN);
		UserDao lvDao = new UserDao();
		lvDao.queryDocument(new JsonObject().put(FieldName.USERNAME, username).put(FieldName.PIN, pin), handler->{
			if(handler.result()!=null) {
				if(!handler.result().isEmpty()) {
					this.CompleteGenerateResponse(CodeMapping.C0000.name(), CodeMapping.C0000.value(),new JsonObject().put(FieldName.SUCCESS,true ), response);
					return;
				}
			}
			this.CompleteGenerateResponse(CodeMapping.C1111.name(), CodeMapping.C1111.value(),new JsonObject().put(FieldName.SUCCESS,false ), response);
		});
	}

}
