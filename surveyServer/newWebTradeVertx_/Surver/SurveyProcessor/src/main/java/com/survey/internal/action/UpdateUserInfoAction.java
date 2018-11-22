package com.survey.internal.action;

import com.survey.dbservice.dao.UserDao;
import com.survey.utils.FieldName;
import com.survey.utils.MessageDefault;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.UpdateOptions;

public class UpdateUserInfoAction extends InternalSurveyBaseAction {

	@Override
	public void doProccess() {
		//String userID = getMessageBody().getString(FieldName.USERID);
		String username = getMessageBody().getString(FieldName.USERNAME);
		JsonObject newUserInfoData = getMessageBody().getJsonObject(FieldName.DATA);
		UserDao lvDao = new UserDao();

		lvDao.updateDocument(new JsonObject().put(FieldName.USERNAME, username), newUserInfoData, new UpdateOptions(false),
				handler -> {
					if (handler.succeeded()) {
						lvDao.doGetUserInfobyUserName(username);
						lvDao.getMvFutureResponse().setHandler(handler1 -> {
							response.complete(handler1.result());
						});
					} else {
						response.complete(MessageDefault.SessionTimeOut());
					}
				});

	}

}
