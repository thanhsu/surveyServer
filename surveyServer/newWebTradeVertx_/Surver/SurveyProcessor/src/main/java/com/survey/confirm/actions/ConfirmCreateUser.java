package com.survey.confirm.actions;

import com.survey.dbservice.dao.UserDao;
import com.survey.utils.FieldName;
import com.survey.utils.SurveyMailCenter;

import io.vertx.core.json.JsonObject;

public class ConfirmCreateUser extends BaseConfirmAction {

	@Override
	public void doProcess(JsonObject msg) {
		
		String username = msg.getString(FieldName.USERNAME);
		boolean success = msg.getBoolean(FieldName.SUCCESS);
		if (success) {
			UserDao lvDao = new UserDao();
			lvDao.doGetUserInfobyUserName(username);
			lvDao.getMvFutureResponse().setHandler(res -> {
				if (res.succeeded() && res.result() != null) {
					String email = res.result().getJsonObject(FieldName.DATA).getString(FieldName.EMAIL);
					SurveyMailCenter.getInstance().sendEmail(email, "Go-Survey Active Account Complete",
							"Active Account", "Your account activated");
				}
			});
			// SurveyMailCenter.getInstance().sendEmail(to, subject, cc, messageData)
		} else {
			UserDao lvDao = new UserDao();
			lvDao.updateAccountToPendingSate(username, msg.getString(FieldName.CODE));
		}
	}

}
