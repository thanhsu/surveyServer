package com.survey.confirm.actions;

import com.survey.dbservice.dao.UserDao;
import com.survey.utils.FieldName;
import com.survey.utils.GenPIN;
import com.survey.utils.SurveyMailCenter;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.UpdateOptions;

public class ConfirmCreateUser extends BaseConfirmAction {

	@Override
	public void doProcess(JsonObject msg) {
		
		String username = msg.getString(FieldName.USERNAME);
		boolean success = msg.getBoolean(FieldName.SUCCESS);
		if (success) {
			UserDao lvDao = new UserDao();
			String pin = GenPIN.newPin(6);
			lvDao.updateDocument(new JsonObject().put(FieldName.USERNAME,username), new JsonObject().put(FieldName.STATUS,"A").put(FieldName.PIN, pin), new UpdateOptions(false), handler->{});
			UserDao lvDao2 = new UserDao();
			lvDao2.doGetUserInfobyUserName(username);
			lvDao2.getMvFutureResponse().setHandler(res -> {
				if (res.succeeded() && res.result() != null) {
					String email = res.result().getJsonObject(FieldName.DATA).getString(FieldName.EMAIL);
					SurveyMailCenter.getInstance().sendEmail(email, "Go-Survey Active Account Complete",
							"Active Account", "<p>Your account activated</p><p>This is your PIN code: "+pin+"</p>");
				}
			});
			// SurveyMailCenter.getInstance().sendEmail(to, subject, cc, messageData)
		} else {
			UserDao lvDao = new UserDao();
			lvDao.updateAccountToPendingSate(username, msg.getString(FieldName.CODE));
		}
	}

}
