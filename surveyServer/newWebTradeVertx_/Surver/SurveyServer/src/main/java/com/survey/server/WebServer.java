package com.survey.server;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.atmosphere.cpr.ApplicationConfig;
import org.atmosphere.vertx.VertxAtmosphere;

import com.survey.constant.EventBusDiscoveryConst;
import com.survey.push.PushManager;
import com.survey.utils.CodeMapping;
import com.survey.utils.FieldName;
import com.survey.utils.Log;
import com.survey.utils.MessageDefault;
import com.survey.utils.VertxServiceCenter;
import com.survey.utils.controller.MicroServiceVerticle;

import io.netty.handler.codec.base64.Base64;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.JksOptions;
import io.vertx.ext.web.Cookie;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CookieHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;
import io.vertx.servicediscovery.Record;

public class WebServer extends MicroServiceVerticle {
	private HttpServer mvHttpServer;
	private EventBus mvEventBus;
	LocalSessionStore mvLocalSessionStored = null;
	private StaticHandler mvStaticHandler;
	public static final String ItradePrivateCookie = "ItradePrivateCookie";
	private static long ITRADETIMEOUT = 300000;
	private static long ITRADEACTIONTIMEOUT = ITRADETIMEOUT / 2;
	private List<String> LISTCASHMETHODSUBPORT = new ArrayList<>();
	private HashMap<String, String> BANKGATEWAYDISCOVERY = new HashMap<>();

	@Override
	public void init(Vertx vertx, Context context) {
		super.init(vertx, context);
		mvEventBus = vertx.eventBus();
		config().getJsonArray("ListCashMethodSupport").forEach(node -> {
			String[] lst = node.toString().split(",");
			LISTCASHMETHODSUBPORT.add(lst[0]);
			BANKGATEWAYDISCOVERY.put(lst[0], lst[1]);
		});
	}

	@Override
	public void start(Future<Void> startFuture) throws Exception {
		super.start(startFuture);
		int lvPort = config().getInteger("HttpPort") == null ? 80 : config().getInteger("HttpPort");
		long lvSessionTimeout = config().getLong("HttpSessionTimeout");
		HttpServerOptions httpOption = new HttpServerOptions();
		Router router = Router.router(this.vertx);
		mvLocalSessionStored = LocalSessionStore.create(this.vertx);
		router.route().handler(BodyHandler.create()).handler(CookieHandler.create())
				.handler(SessionHandler.create(mvLocalSessionStored).setSessionTimeout(lvSessionTimeout));

		// userinfomation routing
		router.get("/:page").handler(rtx -> {
			String lvName = rtx.pathParam("page") + ".html";
			try {
				if (new File("./webroot/" + lvName).exists()) {
					rtx.response().sendFile("./webroot/" + lvName);
				} else {
					rtx.next();
				}
			} catch (Exception e) {
				rtx.next();
			}
		});
		router.post("/register").handler(this::handlerRegister);
		router.post("/login").handler(this::handlerLogin);

		router.route("/survey/:action").handler(this::handlerSurveyAction);
		router.get("/activeuser").handler(this::handlerActiveAccount);
		router.post("/cash/:method/:action").handler(this::handlerCashAction);
		router.route("/resetpassword/:step").handler(this::handlerResetPassword);
		router.route("/admin/confirm").handler(this::handlerAdminConfirm);
		router.route().handler(RoutingContext -> {
			if (this.mvStaticHandler == null) {
				this.mvStaticHandler = StaticHandler.create("webroot");
				this.mvStaticHandler.setCachingEnabled(false);
				this.mvStaticHandler.setIndexPage("index.html").handle(RoutingContext);
			} else {
				if (RoutingContext.request().path().equals("/signin")) {
					RoutingContext.reroute("/");
				} else {
					this.mvStaticHandler.handle(RoutingContext);
				}
			}
		});
		if (config().getBoolean("enabledSSL")) {
			httpOption.setSsl(true).setKeyStoreOptions(new JksOptions().setPassword(config().getString("SSLPassword"))
					.setPath(config().getString("SSLKey")));
		}

		this.mvHttpServer = vertx.createHttpServer(httpOption);
		Log.println("Starting Service Push", Log.TRANSACTION_LOG);
		VertxAtmosphere.Builder lvBuilder = new VertxAtmosphere.Builder();
		try {
			lvBuilder.resource(PushManager.class).httpServer(this.mvHttpServer).url("/push/:module/:action/:clientID")
					.webroot("webroot").initParam(ApplicationConfig.WEBSOCKET_CONTENT_TYPE, "application/json")
					.vertx(this.vertx).build();
		} catch (Exception e) {
			e.printStackTrace();
		}

		this.mvHttpServer.requestHandler(router::accept).listen(lvPort, res -> {
			if (res.succeeded()) {
				System.out.println("******************Init WTrade Services Listening*******************");
				System.out.println("******************Init WTrade Services Port: " + lvPort + " *************");
			} else {
				System.out.println("******************Init WTrade Services Start Failed*******************");
			}
		});

	}

	private void handlerLogin(RoutingContext pvRtx) {
		// Check Message
		JsonObject messageBody = pvRtx.getBodyAsJson();
		messageBody.put("action", "login");
		discovery.getRecord(
				new JsonObject().put("name", EventBusDiscoveryConst.SURVEYINTERNALPROCESSORTDISCOVERY.toString()),
				resultHandler -> {
					if (resultHandler.succeeded() && resultHandler.result() != null) {
						Record record = resultHandler.result();
						mvEventBus.<JsonObject>send(record.getLocation().getString("endpoint"), messageBody, res -> {
							if (res.succeeded()) {
								JsonObject resp = res.result().body();
								if (resp.getString(FieldName.CODE).equals(CodeMapping.C0000.toString())) {
									loginSuccessResponse(pvRtx, resp, pvRtx.session().id());
								} else {
									doResponseNoRenewCookie(pvRtx, Json.encodePrettily(resp));
								}
							} else {
								doResponse(pvRtx, MessageDefault.RequestFailed(CodeMapping.C1111.toString(),
										res.cause().getMessage()));
							}
						});

					} else {
						doResponse(pvRtx, MessageDefault.RequestFailed(resultHandler.cause().getMessage()));
					}
				});

	}

	private void handlerSurveyAction(RoutingContext pvRtx) {

		// Check Message
		JsonObject messageBody = pvRtx.getBodyAsJson();
		messageBody.put("action", pvRtx.pathParam("action"));
		discovery.getRecord(
				new JsonObject().put("name", EventBusDiscoveryConst.SURVEYINTERNALPROCESSORTDISCOVERY.toString()),
				resultHandler -> {
					if (resultHandler.succeeded() && resultHandler.result() != null) {
						Record record = resultHandler.result();
						mvEventBus.<JsonObject>send(record.getLocation().getString("endpoint"), messageBody, res -> {
							if (res.succeeded()) {
								JsonObject resp = res.result().body();
								doResponseNoRenewCookie(pvRtx, Json.encodePrettily(resp));
							} else {
								doResponse(pvRtx, MessageDefault.RequestFailed(CodeMapping.C1111.toString(),
										res.cause().getMessage()));
							}
						});

					} else {
						doResponse(pvRtx, MessageDefault.RequestFailed(resultHandler.cause().getMessage()));
					}
				});

	}

	public static void doResponse(RoutingContext rtx, JsonObject returnObj) {
		doResponse(rtx, Json.encodePrettily(returnObj));
	}

	public static void doResponse(RoutingContext rtx, String returnObj) {
		// TODO Get Private Cookie and renew with timeout
		try {
			rtx.getCookie(ItradePrivateCookie).setMaxAge(ITRADEACTIONTIMEOUT / 1000);
		} catch (Exception e) {
			// TODO: handle exception
		}
		rtx.response().putHeader("Cache-Control", "no-store, no-cache")
				// prevents Internet Explorer from MIME - sniffing a
				// response away from the declared content-type
				.putHeader("X-Content-Type-Options", "nosniff")
				// Strict HTTPS (for about ~3Months)
				.putHeader("Strict-Transport-Security", "max-age=" + 7884000)
				// IE8+ do not allow opening of attachments in the context of this resource
				.putHeader("X-Download-Options", "noopen")
				// enable XSS for IE
				.putHeader("X-XSS-Protection", "1; mode=block")
				// deny frames
				.putHeader("X-FRAME-OPTIONS", "DENY").putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
				.putHeader("Access-Control-Allow-Origin",
						rtx.request().getHeader("Origin") == null ? "*" : rtx.request().getHeader("Origin"))
				.putHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
				.putHeader("Access-Control-Allow-Credentials", "true")
				.putHeader("Access-Control-Allow-Headers", "content-type").end(returnObj);
	}

	private void loginSuccessResponse(RoutingContext rtx, Object message, String token) {
		if (rtx.getCookie(ItradePrivateCookie) != null) {
			rtx.getCookie(ItradePrivateCookie).setValue(rtx.session().id()).setMaxAge(ITRADEACTIONTIMEOUT / 1000);
		} else {
			Cookie cookie = Cookie.cookie(ItradePrivateCookie, rtx.session().id())
					.setMaxAge(ITRADEACTIONTIMEOUT / 1000);
			rtx.addCookie(cookie);
		}
		rtx.response().putHeader("Authorise", token);
		doResponse(rtx, Json.encodePrettily(message));
	}

	// userid, token. tranid
	// get transaction pay tranid -> verify token -> bankGW
	private void handlerCashAction(RoutingContext rtx) {
		String method = rtx.pathParam("method");
		String action = rtx.pathParam("action");
		if (!LISTCASHMETHODSUBPORT.contains(method)) {
			doResponse(rtx, MessageDefault.ActionNotFound());
			return;
		}
		String discoveryKey = BANKGATEWAYDISCOVERY.get(method);
		JsonObject messageBody = rtx.getBodyAsJson();
		messageBody.put(FieldName.METHOD, method);
		messageBody.put("action", "payment");
		messageBody.put(FieldName.DW, action);
		messageBody.put(FieldName.DISCOVERYKEY, discoveryKey);
		discovery.getRecord(
				new JsonObject().put("name", EventBusDiscoveryConst.SURVEYINTERNALPROCESSORTDISCOVERY.toString()),
				resultHandler -> {
					if (resultHandler.succeeded() && resultHandler.result() != null) {
						Record record = resultHandler.result();
						mvEventBus.<JsonObject>send(record.getLocation().getString("endpoint"), messageBody, res -> {
							if (res.succeeded()) {
								JsonObject resp = res.result().body();
								doResponseNoRenewCookie(rtx, Json.encodePrettily(resp));
							} else {
								doResponse(rtx, MessageDefault.RequestFailed(CodeMapping.C1111.toString(),
										res.cause().getMessage()));
							}
						});
					} else {
						doResponse(rtx, MessageDefault.RequestFailed(resultHandler.cause().getMessage()));
					}
				});
		// discovery.getRecord(new JsonObject().put("name", discoveryKey), resultHandler
		// -> {
		// if (resultHandler.succeeded() && resultHandler.result() != null) {
		// Record record = resultHandler.result();
		// mvEventBus.<JsonObject>send(record.getLocation().getString("endpoint"),
		// messageBody, res -> {
		// if (res.succeeded()) {
		// JsonObject resp = res.result().body();
		// doResponseNoRenewCookie(rtx, Json.encodePrettily(resp));
		// } else {
		// doResponse(rtx,
		// MessageDefault.RequestFailed(CodeMapping.C1111.toString(),
		// res.cause().getMessage()));
		// }
		// });
		//
		// } else {
		// doResponse(rtx,
		// MessageDefault.RequestFailed(resultHandler.cause().getMessage()));
		// }
		// });

	}

	public static void doResponseNoRenewCookie(RoutingContext rtx, String returnObj) {
		rtx.response().putHeader("Cache-Control", "no-store, no-cache")
				// prevents Internet Explorer from MIME - sniffing a
				// response away from the declared content-type
				.putHeader("X-Content-Type-Options", "nosniff")
				// Strict HTTPS (for about ~3Months)
				.putHeader("Strict-Transport-Security", "max-age=" + 7884000)
				// IE8+ do not allow opening of attachments in the context of this resource
				.putHeader("X-Download-Options", "noopen")
				// enable XSS for IE
				.putHeader("X-XSS-Protection", "1; mode=block")
				// deny frames
				.putHeader("X-FRAME-OPTIONS", "DENY").putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
				.putHeader("Access-Control-Allow-Origin",
						rtx.request().getHeader("Origin") == null ? "*" : rtx.request().getHeader("Origin"))
				.putHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
				.putHeader("Access-Control-Allow-Credentials", "true")
				.putHeader("Access-Control-Allow-Headers", "content-type").end(returnObj);
	}

	private void handlerActiveAccount(final RoutingContext rtx) {
		String username = rtx.queryParams().get(FieldName.USERNAME);
		String token = rtx.queryParams().get(FieldName.TOKEN);

		JsonObject request = new JsonObject().put(FieldName.USERNAME, username).put(FieldName.TOKEN, token)
				.put(FieldName.ACTION, "activeaccount");
		VertxServiceCenter.getInstance().getDiscovery().getRecord(
				new JsonObject().put("name", EventBusDiscoveryConst.SURVEYINTERNALPROCESSORTDISCOVERY.toString()),
				resultHandler -> {
					if (resultHandler.succeeded() && resultHandler.result() != null) {
						Record record = resultHandler.result();
						VertxServiceCenter.getEventbus().<JsonObject>send(record.getLocation().getString("endpoint"),
								request, res -> {
									if (res.succeeded()) {
										if (res.result().body().getString(FieldName.CODE)
												.equals(CodeMapping.C0000.toString())) {
											rtx.response().end("Confirm success");
										} else {
											rtx.response().end("Confirm Failed");
										}
									} else {
										rtx.response().end("Confirm Failed");
										/*
										 * doResponse(rtx,
										 * MessageDefault.RequestFailed(resultHandler.cause().getMessage()));
										 */
									}
								});
					} else {
						doResponse(rtx, MessageDefault.RequestFailed(resultHandler.cause().getMessage()));
					}
				});
	}

	private void handlerResetPassword(RoutingContext rtx) {
		String step = rtx.pathParam("step");
		final JsonObject lvMessage = new JsonObject();
		if (rtx.request().method().equals(HttpMethod.POST)) {
			lvMessage.mergeIn(rtx.getBodyAsJson());
		}
		lvMessage.put(FieldName.STEP, step);
		lvMessage.put(FieldName.ACTION, "resetpassword");
		rtx.queryParams().forEach(action -> {
			lvMessage.put(action.getKey(), action.getValue());
		});
		VertxServiceCenter.getInstance().getDiscovery().getRecord(
				new JsonObject().put("name", EventBusDiscoveryConst.SURVEYINTERNALPROCESSORTDISCOVERY.toString()),
				resultHandler -> {
					if (resultHandler.succeeded() && resultHandler.result() != null) {
						Record record = resultHandler.result();
						VertxServiceCenter.getEventbus().<JsonObject>send(record.getLocation().getString("endpoint"),
								lvMessage, res -> {
									if (res.succeeded()) {
										if (res.result().body().getString(FieldName.CODE)
												.equals(CodeMapping.C0000.toString())) {
											rtx.response().end("Confirm success");
										} else {
											rtx.response().end("Confirm Failed");
										}
									} else {
										rtx.response().end("Confirm Failed");
										/*
										 * doResponse(rtx,
										 * MessageDefault.RequestFailed(resultHandler.cause().getMessage()));
										 */
									}
								});
					} else {
						doResponse(rtx, MessageDefault.RequestFailed(resultHandler.cause().getMessage()));
					}
				});
	}

	private void handlerRegister(RoutingContext rtx) {
		// Check Message
		JsonObject messageBody = rtx.getBodyAsJson();
		messageBody.put("action", "register");
		discovery.getRecord(
				new JsonObject().put("name", EventBusDiscoveryConst.SURVEYINTERNALPROCESSORTDISCOVERY.toString()),
				resultHandler -> {
					if (resultHandler.succeeded() && resultHandler.result() != null) {
						Record record = resultHandler.result();
						mvEventBus.<JsonObject>send(record.getLocation().getString("endpoint"), messageBody, res -> {
							if (res.succeeded()) {
								JsonObject resp = res.result().body();
								doResponseNoRenewCookie(rtx, Json.encodePrettily(resp));
							} else {
								doResponse(rtx, MessageDefault.RequestFailed(CodeMapping.C1111.toString(),
										res.cause().getMessage()));
							}
						});

					} else {
						doResponse(rtx, MessageDefault.RequestFailed(resultHandler.cause().getMessage()));
					}
				});
	}

	private void handlerAdminConfirm(RoutingContext rtx) {
		if (rtx.request().method().toString().equalsIgnoreCase("post")) {
			JsonObject message = rtx.getBodyAsJson();
			rtx.response().end("Received");
			VertxServiceCenter.getInstance().getDiscovery().getRecord(
					new JsonObject().put("name", EventBusDiscoveryConst.SURVEYCONFIRMPROCESSORDISCOVERY.toString()),
					resultHandler -> {
						if (resultHandler.succeeded() && resultHandler.result() != null) {
							Record record = resultHandler.result();
							VertxServiceCenter.getEventbus().<JsonObject>send(record.getLocation().getString("endpoint"),
									message, res -> {
										if (res.succeeded()) {
											if (res.result().body().getString(FieldName.CODE)
													.equals(CodeMapping.C0000.toString())) {
												rtx.response().end("Confirm success");
											} else {
												rtx.response().end("Confirm Failed");
											}
										} else {
											rtx.response().end("Confirm Failed");
											/*
											 * doResponse(rtx,
											 * MessageDefault.RequestFailed(resultHandler.cause().getMessage()));
											 */
										}
									});
						} else {
							doResponse(rtx, MessageDefault.RequestFailed(resultHandler.cause().getMessage()));
						}
					});
		} else {

		}
	}
}
