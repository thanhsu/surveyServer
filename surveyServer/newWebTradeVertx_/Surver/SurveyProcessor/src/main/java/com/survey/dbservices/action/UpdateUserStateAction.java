package com.survey.dbservices.action;

import com.survey.dbservice.dao.UserDao;
import com.survey.utils.CodeMapping;
import com.survey.utils.FieldName;

import io.vertx.core.json.JsonObject;

public class UpdateUserStateAction extends BaseAdminServiceAction {

	@Override
	public void doProcess(JsonObject body) {
		String username = body.getString(FieldName.USERNAME);
		String state = body.getString(FieldName.STATUS);
		String action = body.getString(FieldName.ACTION);
		if (action.equals("activeaccount")) {
			String token = body.getString(FieldName.TOKEN);
			UserDao lvUserDao = new UserDao();
			lvUserDao.doGetUserInfobyUserName(username);
			lvUserDao.getMvFutureResponse().setHandler(handler -> {
				if (handler.succeeded() && handler.result() != null) {
					String lvToken = handler.result().getString(FieldName.TOKEN);
					if (lvToken.equals(token)) {
						UserDao pvUserDao = new UserDao();
						pvUserDao.updateStatus(username, "N");
						this.mvResponse = lvUserDao.getMvFutureResponse();
					} else {
						mvResponse.complete(new JsonObject().put(FieldName.CODE, CodeMapping.U000B)
								.put(FieldName.MESSAGE, CodeMapping.U000B.value()));
					}
				} else {
					mvResponse.complete(new JsonObject().put(FieldName.CODE, CodeMapping.U1111).put(FieldName.MESSAGE,
							"User not found"));
				}
			});
			return;
		}
		UserDao lvUserDao = new UserDao();
		lvUserDao.updateStatus(username, state);
		this.mvResponse = lvUserDao.getMvFutureResponse();
	}

}
