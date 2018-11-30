package com.survey.etheproxy;

import com.survey.utils.Log;
import com.survey.utils.VertxServiceCenter;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

public class InitService extends AbstractVerticle {
	@Override
	public void init(Vertx vertx, Context context) {
		super.init(vertx, context);
		Log.setMvConfig(config());
		Log.setSysTrace();
		VertxServiceCenter.getInstance().init(vertx, config().getString("discoveryname"),
				config().getString("discoveryaddress"));
	}

	@Override
	public void start(Future<Void> startFuture) throws Exception {
		super.start(startFuture);
		JsonObject lvConfig = config();
		vertx.deployVerticle(EtheServiceProxy.class, new DeploymentOptions().setConfig(lvConfig), res -> {
			if(res.succeeded()) {
				System.out.println("Deploy EtheServiceProxy Success!");
			}else {
				System.out.println("Deploy EtheServiceProxy fail. Cause: "+ res.cause().getMessage());
			}
		});
		
		vertx.deployVerticle(EtheSocketClient.class, new DeploymentOptions().setConfig(lvConfig), res->{
			if(res.succeeded()) {
				System.out.println("Deploy EtheSocketClient Success!");
			}else {
				System.out.println("Deploy EtheSocketClient fail. Cause: "+ res.cause().getMessage());
			}
		});
	}
}
