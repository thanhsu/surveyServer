package com.survey.internal.action;

import com.survey.ProcessorInit;
import com.survey.constant.EventBusDiscoveryConst;

import com.survey.utils.CodeMapping;
import com.survey.utils.FieldName;
import com.survey.utils.Log;
import com.survey.utils.SurveyMailCenter;
import com.survey.utils.VertxServiceCenter;

import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.Record;

public class RegisterAction extends InternalSurveyBaseAction {

	@Override
	public void doProccess() {
		com.survey.dbservices.action.RegisterAction lvRegister = new com.survey.dbservices.action.RegisterAction();
		lvRegister.doProcess(getMessageBody());
		lvRegister.getMvResponse().setHandler(res -> {
			if (res.succeeded()) {
				JsonObject msg = res.result();
				if (msg.getString(FieldName.CODE).equals(CodeMapping.R0000.toString())) {
					response.complete(msg);
					// Register Success Do Sent Email
					if (SurveyMailCenter.getInstance()
							.sendEmail(getMessageBody().getString(FieldName.EMAIL), "Register-ISurvey", "",
									"Register success. Access your account by link: "
											+ generateactivelink(getMessageBody().getString(FieldName.USERNAME),
													msg.getString(FieldName.TOKEN)))) {
						// Sent Mail
					}
				} else {
					response.complete(msg);
				}
			} else {
				response.fail(res.cause());
			}
			response.completer();
		});
	}

	private String generateactivelink(String username, String token) {
		String lvTemp = ProcessorInit.mvConfig.getString("SurveyHost") + "/activeuser?username=" + username;
		try {
			lvTemp =lvTemp+ "&token=" + token;
			JsonObject tmp = new JsonObject().put(FieldName.USERNAME, username).put(FieldName.TOKEN, token);
			VertxServiceCenter.getInstance().getSharedData().getClusterWideMap(VertxServiceCenter.RegisterSharedData,
					resultHandler -> {
						resultHandler.result().put(username, tmp, completionHandler -> {
							if (completionHandler.succeeded()) {
								Log.print("Caching register request success for username:  " + username,
										Log.ACCESS_LOG);
							} else {
								Log.print("Caching register request failed. Cause:"
										+ completionHandler.cause().getMessage());
							}
						});
					});
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return lvTemp;
	}

}