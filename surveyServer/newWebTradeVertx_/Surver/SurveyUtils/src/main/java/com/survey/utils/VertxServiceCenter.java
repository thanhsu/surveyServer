package com.survey.utils;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.AsyncMap;
import io.vertx.core.shareddata.SharedData;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.ServiceDiscoveryOptions;

public class VertxServiceCenter {
	private static VertxServiceCenter instance;
	private static Vertx vertx;
	private static EventBus eventbus;
	private static SharedData sharedData;
	protected ServiceDiscovery discovery;
	private String discoveryName;
	private String discoveryKey;
	private AsyncMap<String, JsonObject> memorySurveyData;
	public static final String LoadSurveyData = "SHARED.SURVEYDATA";
	public static final String RegisterSharedData = "SHARED.SURVEYREGISTER";
	public static final String ResetPasswordSharedData = "SHARED.SURVEYRESETPASSWORD";
	public static final String TempPasswordSharedData = "SHARED.SURVEYTEMPPASSWORD";

	public static VertxServiceCenter getInstance() {
		if (instance == null) {
			synchronized (VertxServiceCenter.class) {
				instance = new VertxServiceCenter();
			}
		}
		return instance;
	}

	public void init(Vertx vertx, String disName, String disKey) {
		setVertx(vertx);
		setDiscoveryKey(disKey);
		setDiscoveryName(disName);
		discovery = ServiceDiscovery.create(vertx,
				new ServiceDiscoveryOptions().setAnnounceAddress(getDiscoveryKey()).setName(getDiscoveryName()));
		eventbus = vertx.eventBus();
		sharedData = vertx.sharedData();
	}

	public void sendNewMessage(String discoveryName, JsonObject eventBusMessage, Future<JsonObject> responseHandler) {
		VertxServiceCenter.getInstance().getDiscovery().getRecord(new JsonObject().put("name", discoveryName),
				new Handler<AsyncResult<Record>>() {
					@Override
					public void handle(AsyncResult<Record> event) {
						if (event.succeeded() && event.result() != null) {
							Record record = event.result();
							VertxServiceCenter.getEventbus().<JsonObject>send(
									record.getLocation().getString("endpoint"), eventBusMessage, new DeliveryOptions().setSendTimeout(100000),rs -> {
										if (rs.succeeded()) {
											JsonObject lvRes = new JsonObject();
											if (rs.result().body().getString(FieldName.CODE).equals("E200")) {
												lvRes.put(FieldName.CODE, CodeMapping.P0000.name());
												lvRes.put(FieldName.MESSAGE, CodeMapping.P0000.value()).put(
														FieldName.DATA,
														rs.result().body().getJsonObject(FieldName.DATA));

												responseHandler.complete(lvRes);
											} else {
												lvRes.put(FieldName.CODE, CodeMapping.P2222.name());
												lvRes.put(FieldName.MESSAGE, CodeMapping.P2222.value())
														.put(FieldName.DATA, rs.result().body());
												responseHandler.complete(lvRes);
											}
										} else {
											responseHandler.fail(rs.cause().getMessage());
										}
									});
						} else {
							responseHandler.fail(event.cause());
							/* responseHandler.completer(); */
						}
					}
				});
	}

	public Future<JsonObject> getSurveyCache(String surveyID) {
		Future<JsonObject> lvFuture = Future.future();
		sharedData.getClusterWideMap(LoadSurveyData, resultHandler -> {
			if (resultHandler.succeeded() & resultHandler.result() != null) {
				resultHandler.result().get(surveyID, sv -> {
					if (sv.succeeded()) {
						lvFuture.complete((JsonObject) sv.result());
					} else {
						lvFuture.fail("Null");
					}
				});
			} else {
				lvFuture.fail("Null");
			}
		});
		return lvFuture;
	}

	public void cachingSurveyData(String surveyID, JsonObject data) {
		sharedData.getClusterWideMap(LoadSurveyData, resultHandler -> {
			if (resultHandler.succeeded() & resultHandler.result() != null) {
				resultHandler.result().replace(surveyID, data, completionHandler -> {
					if (resultHandler.succeeded()) {
						Log.print("Caching survey data: " + data);
					}
				});
			}
		});
	}

	public static Vertx getVertx() {
		return vertx;
	}

	public static void setVertx(Vertx vertx) {
		VertxServiceCenter.vertx = vertx;
	}

	public static EventBus getEventbus() {
		return eventbus;
	}

	public static void setEventbus(EventBus eventbus) {
		VertxServiceCenter.eventbus = eventbus;
	}

	public SharedData getSharedData() {
		return sharedData;
	}

	public void setSharedData(SharedData sharedData) {
		VertxServiceCenter.sharedData = sharedData;
	}

	public ServiceDiscovery getDiscovery() {
		return discovery;
	}

	public void setDiscovery(ServiceDiscovery discovery) {
		this.discovery = discovery;
	}

	public void setInstance(VertxServiceCenter instance) {
		this.instance = instance;
	}

	public String getDiscoveryName() {
		return discoveryName;
	}

	public void setDiscoveryName(String discoveryName) {
		this.discoveryName = discoveryName;
	}

	public String getDiscoveryKey() {
		return discoveryKey;
	}

	public void setDiscoveryKey(String discoveryKey) {
		this.discoveryKey = discoveryKey;
	}
}
