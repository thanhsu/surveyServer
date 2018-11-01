package com.survey.internal.action;

import com.survey.constant.EventBusDiscoveryConst;
import com.survey.constant.HttpParameter;
import com.survey.utils.CodeMapping;
import com.survey.utils.FieldName;
import com.survey.utils.MessageDefault;
import com.survey.utils.VertxServiceCenter;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.servicediscovery.Record;

public class LoginAction extends BaseSurveyInternalAction {

	public LoginAction() {
	}

	public LoginAction(RoutingContext rtx) {
		super(rtx);
	}

	@Override
	public void doProccess() {
		//response = Future.future();
		loginID = getMessageBody().getString(HttpParameter.LOGINID);
		String lvPass = getMessageBody().getString(HttpParameter.PASSWORD);
		// Check Message Login

		VertxServiceCenter.getInstance().getDiscovery().getRecord(
				new JsonObject().put("name", EventBusDiscoveryConst.SURVEYDBDISCOVERY.toString()), resultHandler -> {
					if (resultHandler.succeeded() && resultHandler.result() != null) {
						Record record = resultHandler.result();
						VertxServiceCenter.getEventbus().<JsonObject>send(record.getLocation().getString("endpoint"),
								getMessageBody(), res -> {
									if (res.succeeded()) {
										JsonObject msg = res.result().body();
										if (msg.getString(FieldName.CODE).equals(CodeMapping.C0000)) {
											response.complete(msg);
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
