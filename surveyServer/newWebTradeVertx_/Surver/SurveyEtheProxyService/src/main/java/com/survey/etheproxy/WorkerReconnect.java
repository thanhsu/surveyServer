package com.survey.etheproxy;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.WorkerExecutor;

public class WorkerReconnect extends AbstractVerticle {
	@Override
	public void init(Vertx vertx, Context context) {
		super.init(vertx, context);
		long interval = config().getLong("EtheInterval")==null?60000:config().getLong("EtheInterval");
		Reconnect.interval = interval;
	}
	@Override
	public void start(Future<Void> startFuture) throws Exception {
			super.start(startFuture);
			WorkerExecutor executor = vertx.createSharedWorkerExecutor("my-worker-pool");
			executor.executeBlocking(future -> {
			  // Call some blocking API that takes a significant amount of time to return
				Reconnect t= new Reconnect();
				t.reConnect();
			}, res -> {
			  System.out.println("The result is: " + res.result());
			});
			
	}
}
