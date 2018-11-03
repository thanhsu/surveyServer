package com.survey;

import java.util.HashMap;
import java.util.Map;

import com.survey.constant.EventBusDiscoveryConst;
import com.survey.utils.FieldName;
import com.survey.utils.MessageDefault;
import com.survey.utils.controller.MicroServiceVerticle;

import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;

public class ProxyEthereumServer extends MicroServiceVerticle {
	private WebClient webClient;
	private String domain;
	private Boolean isSSL;
	private String host;
	private int port;
	private Map<String, String> MAPPINGURI = new HashMap<>();

	@Override
	public void init(Vertx vertx, Context context) {
		// TODO Auto-generated method stub
		super.init(vertx, context);
		webClient = WebClient.create(vertx);

		initMappingUri(config().getJsonObject("EthereumURIMapping"));
		domain = config().getString("EthereumDomain");
		isSSL = config().getBoolean("EthereumIsSSL");
		host = config().getString("EthereumHost");
		port = config().getInteger("EthereumPort");
	}

	@Override
	public void start(Future<Void> startFuture) throws Exception {
		super.start(startFuture);
		vertx.eventBus().<JsonObject>consumer(EventBusDiscoveryConst.ETHEREUMPROXYDISCOVERY.value())
				.handler(handler -> {
					JsonObject messageBody = handler.body();
					String action = messageBody.getString(FieldName.ACTION);
					String method = messageBody.getString(FieldName.METHOD);
					switch (method.toUpperCase()) {
					case "GET":
						handlerMethodGet(action, messageBody.getJsonObject(FieldName.DATA), handler);
						break;
					case "POST":
						handlerMethodPost(action, messageBody.getJsonObject(FieldName.DATA), handler);
						break;
					default:
						handler.reply(MessageDefault.MethodNotSupport());
					}
				});

		this.publishEventBusService(EventBusDiscoveryConst.ETHEREUMPROXYDISCOVERY.toString(),
				EventBusDiscoveryConst.ETHEREUMPROXYDISCOVERY.value(), completionHandler -> {
					if (completionHandler.failed()) {
						System.out
								.println("Pushlish Eventbus Failed. Cause: " + completionHandler.cause().getMessage());
					}
				});
	}

	private void handlerMethodGet(String action, JsonObject data, Message<JsonObject> message) {
		webClient = WebClient.create(vertx);
		String uri = MAPPINGURI.get(action);
		if (uri == null) {
			// Do action not found
			message.reply(MessageDefault.ActionNotFound());
		} else {
			String url = domain;
			if (data != null) {
				Boolean haveCondination = false;
				for (String entry : data.fieldNames()) {
					if (data.getValue(entry) != null) {
						uri += (haveCondination ? "?" : "&") + entry + "=" + data.getValue(entry).toString();
					}
				}
			}
			if (isSSL) {
				webClient.getAbs(url + uri).send(r -> {
					JsonObject resp = new JsonObject();
					resp.put(FieldName.HTTPSTATUS, r.result().statusCode());
					resp.put(FieldName.DATA, r.result().bodyAsJsonObject());
					resp.put(FieldName.HTTPHEADERS, r.result().headers());
					message.reply(resp);
				});
			} else {
				webClient.get(port, host, uri).send(r -> {
					JsonObject resp = new JsonObject();
					resp.put(FieldName.HTTPSTATUS, r.result().statusCode());
					resp.put(FieldName.DATA, r.result().bodyAsJsonObject());
					resp.put(FieldName.HTTPHEADERS, r.result().headers());
					message.reply(resp);
				});
			}

		}
	}

	private void handlerMethodPost(String action, JsonObject data, Message<JsonObject> message) {
		webClient = WebClient.create(vertx);
		String uri = MAPPINGURI.get(action);
		if (uri == null) {
			// Do action not found
			message.reply(MessageDefault.ActionNotFound());
		} else {
			if (isSSL) {
				webClient.postAbs(domain + uri).sendJsonObject(data, r -> {
					JsonObject resp = new JsonObject();
					resp.put(FieldName.HTTPSTATUS, r.result().statusCode());
					resp.put(FieldName.DATA, r.result().bodyAsJsonObject());
					resp.put(FieldName.HTTPHEADERS, r.result().headers());
					message.reply(resp);
				});
				;
			} else {
				webClient.post(port, host, uri).sendJsonObject(data, r -> {
					JsonObject resp = new JsonObject();
					resp.put(FieldName.HTTPSTATUS, r.result().statusCode());
					resp.put(FieldName.DATA, r.result().bodyAsJsonObject());
					resp.put(FieldName.HTTPHEADERS, r.result().headers());
					message.reply(resp);
				});
			}
		}
	}

	private void initMappingUri(JsonObject config) {
		config.fieldNames().forEach(actionName -> {
			try {
				MAPPINGURI.put(actionName, config.getString(actionName));
			} catch (Exception e) {
				// TODO: handle exception
			}
		});
	}

	public WebClient getWebClient() {
		return webClient;
	}

	public void setWebClient(WebClient webClient) {
		this.webClient = webClient;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public Boolean getIsSSL() {
		return isSSL;
	}

	public void setIsSSL(Boolean isSSL) {
		this.isSSL = isSSL;
	}
}
