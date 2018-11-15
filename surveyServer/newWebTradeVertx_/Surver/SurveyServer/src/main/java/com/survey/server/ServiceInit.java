package com.survey.server;

import com.survey.utils.Log;
import com.survey.utils.VertxServiceCenter;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;

public class ServiceInit extends AbstractVerticle {
	@Override
	public void init(Vertx vertx, Context context) {
		super.init(vertx, context);
		Log.setMvConfig(config());
		Log.setSysTrace();
	}

	@Override
	public void start(Future<Void> startFuture) throws Exception {
		super.start(startFuture);
		VertxServiceCenter.getInstance().init(vertx, config().getString("discoveryname"),
				config().getString("discoveryaddress"));
		vertx.deployVerticle(WebServer.class.getName(), new DeploymentOptions().setConfig(config()));
		
		vertx.deployVerticle(PushServer.class.getName(), new DeploymentOptions().setConfig(config()));
	}
}
