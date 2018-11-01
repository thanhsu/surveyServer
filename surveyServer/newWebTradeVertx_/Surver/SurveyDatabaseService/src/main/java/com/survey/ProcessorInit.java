package com.survey;

import com.survey.dbservices.DBServiceInit;
import com.survey.utils.Log;
import com.survey.utils.RSAEncrypt;
import com.survey.utils.SurveyMailCenter;
import com.survey.utils.VertxServiceCenter;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

public class ProcessorInit extends AbstractVerticle {
	public static JsonObject mvConfig;

	@Override
	public void init(Vertx vertx, Context context) {
		super.init(vertx, context);
		mvConfig = config();
		Log.setMvConfig(config());
		Log.setSysTrace();
		VertxServiceCenter.getInstance().init(vertx, config().getString("discoveryname"),
				config().getString("discoveryaddress"));
		SurveyMailCenter.getInstance().init(mvConfig.getJsonObject("EmailServiceConfig"), vertx);
		RSAEncrypt.getIntance().init(config().getJsonObject("RSAEncrypt"));
	}

	@Override
	public void start(Future<Void> startFuture) throws Exception {
		super.start(startFuture);
		DeploymentOptions lvDeployInternalProcessorOption = new DeploymentOptions();
		lvDeployInternalProcessorOption.setConfig(config());
		lvDeployInternalProcessorOption
				.setWorker(config().getJsonObject("ServiceInternalProcessorConfig").getBoolean("isWorker"))
				.setInstances(config().getJsonObject("ServiceInternalProcessorConfig").getInteger("instance"));

		vertx.deployVerticle(InternalProcessorVerticle.class, lvDeployInternalProcessorOption, res -> {
			if (res.succeeded()) {
				Log.print("InternalProcessorVerticle init Success");
			} else {
				Log.print("InternalProcessorVerticle init Failed. Cause: " + res.cause().getMessage());
				System.out.println("InternalProcessorVerticle init Failed. Cause: " + res.cause().getMessage());
				System.exit(-1);
				vertx.close();
			}
		});

		vertx.deployVerticle(DBServiceInit.class, new DeploymentOptions().setConfig(config()), h -> {
			if (h.succeeded()) {
				Log.print("DBServiceInit init Success");
			} else {
				Log.print("DBServiceInit init Failed. Cause: " + h.cause().getMessage());
				System.out.println("DBServiceInit init Failed. Cause: " + h.cause().getMessage());
				System.exit(-1);
				vertx.close();
			}
		});
		// vertx.deployVerticle(ServiceMailCenter.class, new
		// DeploymentOptions().setConfig(config()));

	}
}
