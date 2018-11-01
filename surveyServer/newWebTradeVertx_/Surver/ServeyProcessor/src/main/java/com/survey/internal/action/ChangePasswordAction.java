package com.survey.internal.action;

import com.survey.constant.EventBusDiscoveryConst;
import com.survey.constant.HttpParameter;
import com.survey.utils.CodeMapping;
import com.survey.utils.FieldName;
import com.survey.utils.SurveyMailCenter;
import com.survey.utils.VertxServiceCenter;

import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.Record;

public class ChangePasswordAction extends BaseSurveyInternalAction {

	@Override
	public void doProccess() {
		VertxServiceCenter.getInstance().getDiscovery().getRecord(
				new JsonObject().put("name", EventBusDiscoveryConst.SURVEYDBDISCOVERY.toString()), resultHandler -> {
					if (resultHandler.succeeded() && resultHandler.result() != null) {
						Record record = resultHandler.result();
						VertxServiceCenter.getEventbus().<JsonObject>send(record.getLocation().getString("endpoint"),
								getMessageBody(), res -> {
									if (res.succeeded()) {
										JsonObject msg = res.result().body();
										if (msg.getString(FieldName.CODE).equals(CodeMapping.R0000.toString())) {
											response.complete(msg);
											// Register Success Do Sent Email
											if (SurveyMailCenter.getInstance().sendEmail(
													getMessageBody().getString(FieldName.EMAIL), "Register-ISurvey", "",
													"Register success. Access your account by link")) {

											}
										} else {
											response.complete(msg);
										}
									} else {
										response.fail(res.cause());
									}
									response.completer();
								});
					} else {
						response.fail(resultHandler.cause());

						response.completer();
					}
				});

	}

}
