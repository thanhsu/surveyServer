package com.survey.internal.action;

import com.survey.constant.EventBusDiscoveryConst;
import com.survey.dbservice.dao.ProxyLogDao;
import com.survey.dbservice.dao.UserDao;
import com.survey.etheaction.ProxyActiveUser;
import com.survey.utils.CodeMapping;
import com.survey.utils.FieldName;
import com.survey.utils.MessageDefault;
import com.survey.utils.VertxServiceCenter;

import io.vertx.core.Future;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;

public class ActiveUserAction extends InternalSurveyBaseAction {

	@Override
	public void doProccess() {
		UserDao lvUserDao = new UserDao();
		lvUserDao.doGetUserInfobyUserName(getMessageBody().getString(FieldName.USERNAME));
		String token = getMessageBody().getString(FieldName.TOKEN);
		lvUserDao.getMvFutureResponse().setHandler(handler -> {
			if (handler.succeeded() && handler.result() != null) {
				if (!handler.result().getString(FieldName.CODE).equals(CodeMapping.U0000.toString())) {
					response.complete(
							MessageDefault.RequestFailed(CodeMapping.U000A.toString(), CodeMapping.U000A.value()));
				} else {
					if (token.equals(handler.result().getJsonObject(FieldName.DATA).getString(FieldName.TOKEN))) {
						UserDao lvUserDao1 = new UserDao();
						final String username = getMessageBody().getString(FieldName.USERNAME);
						final String email = getMessageBody().getString(FieldName.EMAIL);
						lvUserDao1.updateStatus(getMessageBody().getString(FieldName.USERNAME), "P");
						lvUserDao1.getMvFutureResponse().setHandler(handlerx -> {
							response.complete(handlerx.result());
							Future<JsonObject> lvProxyResult = Future.future();
							JsonObject rq = new JsonObject().put(FieldName.ACTION, "createaccount")
									.put(FieldName.USERNAME, username).put(FieldName.EMAIL, email);

							lvProxyResult.setHandler(x -> {
								ProxyLogDao lvDao = new ProxyLogDao();
								lvDao.storeNewRequest("createaccount", rq, lvProxyResult.result());
								if (lvProxyResult.result() != null) {
									String code = lvProxyResult.result().getString(FieldName.CODE) == null ? ""
											: lvProxyResult.result().getString(FieldName.CODE);
									if (!code.equals("P0000")) {
										UserDao lvUserDao2 = new UserDao();
										lvUserDao2.updateStatus(username, "R");
									} else {
										System.out.println("Request create user success"+ Json.encode(lvProxyResult.result()));
									}
								}
							});

							ProxyActiveUser lvProxyActiveUser = new ProxyActiveUser(username, email);
							lvProxyActiveUser.sendToProxyServer(lvProxyResult);
						});
					} else {
						response.complete(
								MessageDefault.RequestFailed(CodeMapping.U000A.toString(), CodeMapping.U000A.value()));
					}
				}
			} else {
				response.complete(
						MessageDefault.RequestFailed(CodeMapping.U000A.toString(), CodeMapping.U000A.value()));
			}
		});
	}

}
