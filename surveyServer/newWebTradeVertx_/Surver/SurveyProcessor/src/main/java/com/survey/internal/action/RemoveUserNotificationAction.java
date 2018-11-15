package com.survey.internal.action;

import com.survey.dbservice.dao.UserNotificationDao;
import com.survey.utils.CodeMapping;
import com.survey.utils.FieldName;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class RemoveUserNotificationAction extends InternalSurveyBaseAction {

	@Override
	public void doProccess() {
		String username = getMessageBody().getString(FieldName.USERNAME);
		JsonArray lstNotifi = getMessageBody().getJsonArray(FieldName.LISTNOTIFICATION);
		UserNotificationDao lvDao = new UserNotificationDao();
		lvDao.deleteNotification(username, lstNotifi);
		this.CompleteGenerateResponse(CodeMapping.C0000.toString(), "", new JsonObject().put("success", true),
				response);
	}

}
