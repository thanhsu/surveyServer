package com.survey.etheproxy;

import java.util.HashMap;

import com.survey.constant.EventBusDiscoveryConst;
import com.survey.utils.FieldName;
import com.survey.utils.controller.MicroServiceVerticle;

import io.vertx.circuitbreaker.CircuitBreaker;
import io.vertx.circuitbreaker.CircuitBreakerOptions;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.WebClient;

public class EtheServiceProxy extends MicroServiceVerticle {
	public static String etheIP;
	public static int ethePort;
	public boolean isSSL;
	public static HashMap<String, String> actionMapping = new HashMap<>();
	public static HashMap<String, String> methodActionMapping = new HashMap<>();

	@Override
	public void init(Vertx vertx, Context context) {
		super.init(vertx, context);
		JsonArray lvActionMapping = config().getJsonArray("ActionLinkMapping");
		lvActionMapping.forEach(node -> {
			String[] lvTmp = ((String) node).split(",");
			actionMapping.put(lvTmp[1], "/"+lvTmp[2]);
			methodActionMapping.put(lvTmp[1], lvTmp[0]);
		});
		etheIP = config().getString("EtheServerIP");
		ethePort = config().getInteger("EtheServerPort");
		isSSL = config().getBoolean("EtheServerIsSSL");
	}

	@Override
	public void start(Future<Void> startFuture) throws Exception {
		super.start(startFuture);

		vertx.eventBus().<JsonObject>consumer(EventBusDiscoveryConst.ETHEREUMPROXYDISCOVERY.value(), h -> {
			String actionName = h.body().getString(FieldName.ACTION);
			String method = methodActionMapping.get(actionName);
			String uri = actionMapping.get(actionName);
			JsonObject body = h.body().getJsonObject(FieldName.DATA);
			WebClient webClient = WebClient.create(vertx);
			CircuitBreaker breaker = CircuitBreaker.create("my-circuit-breaker", vertx,
					new CircuitBreakerOptions().setMaxFailures(5) // number of failure before opening the circuit
							.setTimeout(20000) // consider a failure if the operation does not succeed in time
							.setFallbackOnFailure(true) // do we call the fallback on failure
							.setResetTimeout(10000) // time spent in open state before attempting to re-try
			);
			breaker.execute(future -> {
				HttpRequest<Buffer> httpRequest;
				if (method.equalsIgnoreCase("POST")) {
					if (isSSL) {
						httpRequest = webClient.postAbs("https://" + etheIP + ":" + ethePort + "/" + uri);
					} else {
						httpRequest = webClient.post(ethePort, etheIP, uri);
					}
					httpRequest.headers().add("clientid", "web_server");
					httpRequest.sendJsonObject(body, handler -> {
						if (handler.succeeded()) {
							h.reply(handler.result().bodyAsJsonObject());
							future.complete(handler.result().bodyAsBuffer());
						} else {
							h.reply(new JsonObject().put(FieldName.CODE, "P1111").put(FieldName.MESSAGE,
									handler.cause().getMessage()));
							future.fail(handler.cause().getMessage());
						}
					});
				} else {
					if (isSSL) {
						httpRequest = webClient.getAbs("https://" + etheIP + ":" + ethePort + "/" + uri);
					} else {
						httpRequest = webClient.get(ethePort, etheIP, uri);
					}
					httpRequest.headers().add("clientid", "web_server");
					httpRequest.sendJsonObject(body, handler -> {
						if (handler.succeeded()) {
							h.reply(handler.result().bodyAsJsonObject());
							future.complete(handler.result().bodyAsBuffer());
						} else {
							future.fail(handler.cause());
						}
					});
				}
			}).setHandler(ar -> {
				try {
					webClient.close();
				} catch (Exception e) {
				}
			});
		});

		publishEventBusService(EventBusDiscoveryConst.ETHEREUMPROXYDISCOVERY.name(),
				EventBusDiscoveryConst.ETHEREUMPROXYDISCOVERY.value(), completionHandler -> {
					if (completionHandler.succeeded()) {
						System.out
								.println("Pushlish successed " + EventBusDiscoveryConst.ETHEREUMPROXYDISCOVERY.name());
					} else {
						System.out.println("Pushlish failed " + EventBusDiscoveryConst.ETHEREUMPROXYDISCOVERY.name()
								+ " Cause: " + completionHandler.cause().getMessage());
					}
				});
	}
}
