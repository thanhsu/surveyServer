package com.survey.server;

import com.survey.utils.controller.MicroServiceVerticle;

import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Vertx;

public class ServiceMailCenter extends MicroServiceVerticle {
	@Override
	public void init(Vertx vertx, Context context) {
		super.init(vertx, context);
	}

	@Override
	public void start(Future<Void> startFuture) throws Exception {
		super.start(startFuture);
		// Depoy Vertx Mail Service

	}
}
