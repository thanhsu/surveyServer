package com.survey.server;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.atmosphere.cpr.ApplicationConfig;
import org.atmosphere.vertx.VertxAtmosphere;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.survey.constant.EventBusDiscoveryConst;
import com.survey.push.PushManager;
import com.survey.utils.CodeMapping;
import com.survey.utils.FieldName;
import com.survey.utils.Log;
import com.survey.utils.MessageDefault;
import com.survey.utils.VertxServiceCenter;
import com.survey.utils.controller.MicroServiceVerticle;

import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.JksOptions;
import io.vertx.ext.web.Cookie;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CookieHandler;
import io.vertx.ext.web.handler.CorsHandler;
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
	private JsonObject mvWebConfig = new JsonObject();
	private Buffer Webroot = null;
	private boolean isDebug = false;
	public static JsonArray LISTACTIONALLOWANONYMOUS = new JsonArray();
	String fbAppID;

	@Override
	public void init(Vertx vertx, Context context) {
		super.init(vertx, context);
		mvEventBus = vertx.eventBus();
		config().getJsonArray("ListCashMethodSupport").forEach(node -> {
			String[] lst = node.toString().split(",");
			LISTCASHMETHODSUBPORT.add(lst[0]);
			BANKGATEWAYDISCOVERY.put(lst[0], lst[1]);
		});
		isDebug = config().getBoolean("DebugMode") == null ? false : config().getBoolean("DebugMode");
		LISTACTIONALLOWANONYMOUS = config().getJsonArray("ListActionAllowAnonymous");
		fbAppID = config().getString("FbAppID") == null ? "497321087444409" : config().getString("FbAppID");
	}

	@Override
	public void start(Future<Void> startFuture) throws Exception {
		super.start(startFuture);
		int lvPort = config().getInteger("HttpPort") == null ? 80 : config().getInteger("HttpPort");
		long lvSessionTimeout = config().getLong("HttpSessionTimeout");
		HttpServerOptions httpOption = new HttpServerOptions();
		Router router = Router.router(this.vertx);

		Set<String> allowedHeaders = new HashSet<>();
		allowedHeaders.add("Access-Control-Allow-Origin");
		allowedHeaders.add("origin");
		allowedHeaders.add("Content-Type");
		allowedHeaders.add("accept");

		Set<HttpMethod> allowedMethods = new HashSet<>();
		allowedMethods.add(HttpMethod.GET);
		allowedMethods.add(HttpMethod.POST);
		allowedMethods.add(HttpMethod.OPTIONS);
		allowedMethods.add(HttpMethod.DELETE);
		allowedMethods.add(HttpMethod.PATCH);
		allowedMethods.add(HttpMethod.PUT);

		mvLocalSessionStored = LocalSessionStore.create(this.vertx);
		router.route().handler(BodyHandler.create()).handler(CookieHandler.create())
				.handler(SessionHandler.create(mvLocalSessionStored).setSessionTimeout(lvSessionTimeout))
				.handler(CorsHandler.create("*").allowedHeaders(allowedHeaders).allowedMethods(allowedMethods));

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
		router.route("/api/register").handler(this::handlerRegister);
		router.route("/api/login").handler(this::handlerLogin);
		router.post("/m/login").handler(this::handlerLogin);

		router.route("/api/survey/:action").handler(this::handlerSurveyAction);
		router.get("/api/activeuser").handler(this::handlerActiveAccount);
		router.post("/api/cash/:method/:action").handler(this::handlerCashAction);
		router.route("/api/cash/confirm/:method/:action").handler(this::handlerCashConfirm);
		router.route("/api/resetpassword/:step").handler(this::handlerResetPassword);
		router.route("/api/admin/confirm").handler(this::handlerAdminConfirm);

		router.route("/api/image/:action").handler(this::handlerSurveyImage);

		router.route("/survey/:id").handler(this::handlerGetSurvey);

		router.route("/m/logout").handler(this::handlerLogout);
		router.post("/api/logout").handler(this::handlerLogout);

		router.route("/hero/*").handler(pRoutingContext -> {
			responseHomeIndex(pRoutingContext);
		});

		router.route("/explorer").handler(this::handlerExploer);

		router.get("/test/:message").handler(rtx -> {
			rtx.response().end("OK");
			mvEventBus.send(EventBusDiscoveryConst.SURVEYPUSHPRIVATESERVERDISCOVEY.value(),
					new JsonObject().put("action", "notification").put("session", "thanhsu604@gmail.com").put("data",
							new JsonObject().put("surveyId", "123").put("message", rtx.pathParam("message"))),
					h -> {
						if (h.succeeded()) {
							System.out.println("Success");
						}
					});
		});

		router.get("/m/*").handler(pRoutingContext -> {
			Session session = pRoutingContext.session();
			if (session == null) {
				redirectTo(pRoutingContext, "/login");
				return;
			}
			if (session.data().get(FieldName.USERNAME) != null) {
				// RoutingContext.response().sendFile("./webroot/index.html");
				responseHomeIndex(pRoutingContext);
			} else {
				redirectTo(pRoutingContext, "/login");
			}
		});
		router.get("/m/:s").handler(pRoutingContext -> {
			Session session = pRoutingContext.session();
			if (session == null) {
				redirectTo(pRoutingContext, "/login");
				return;
			}
			/*
			 * if (session.data().isEmpty()) { //
			 * pRoutingContext.response().sendFile("./webroot/index.html");
			 * redirectTo(pRoutingContext, "/login"); } else
			 */
			if (session.data().get(FieldName.USERNAME) == null) {
				redirectTo(pRoutingContext, "/login");
			} else {
				responseHomeIndex(pRoutingContext);
			}
		});

		router.get("/").handler(RoutingContext -> {
			Session session = RoutingContext.session();
			if (!session.data().isEmpty()) {
				redirectTo(RoutingContext, "/m");
			} else {
				vertx.fileSystem().readFile("./webroot/home.html", handler -> {
					if (handler.succeeded() && handler.result() != null) {
						Buffer z;
						io.vertx.core.buffer.Buffer bf = handler.result();
						try {
							Document doc = Jsoup.parse(bf.toString(), "UTF-8");
							String ggHtml = "<meta charset=\"utf-8\">\r\n"
									+ "    <meta name=\"google-site-verification\" content=\"UgBZWyHadggxerdmIv2ADcJv78UWnY0E9LK6YjlbkVw\"/>"
									+ "    <meta name=\"robots\" content=\"all\">";

							String fbHtml = "<meta property=\"og:url\"  content=\""
									+ RoutingContext.request().absoluteURI() + "\" />\r\n"
									+ "<meta property=\"fb:app_id\"          content=\"" + config() + "\" /> "
									+ "<meta property=\"og:title\"  content=\"Go Survey\" />\r\n"
									+ "<meta property=\"og:description\" content=\"Go Survey\" />\r\n";
							doc.head().append(ggHtml);
							doc.head().append(fbHtml);
							z = Buffer.buffer(doc.html());
						} catch (Exception e) {
							z = bf;
						}
						RoutingContext.response().setWriteQueueMaxSize(z.length() * 2);
						RoutingContext.response().putHeader("Content-Type", "text/html;charset=UTF-8");
						RoutingContext.response().putHeader("Content-Length", (z.length()) + "");
						RoutingContext.response().write(z).end();
					}
				});
			}
		});

		router.get("/test").handler(rtx -> {
			responseHomeIndex(rtx);
		});

		router.route().handler(RoutingContext -> {
			if (this.mvStaticHandler == null) {
				this.mvStaticHandler = StaticHandler.create("webroot");
				this.mvStaticHandler.setCachingEnabled(false);
				this.mvStaticHandler.setIndexPage("home.html").handle(RoutingContext);
			} else {
				this.mvStaticHandler.handle(RoutingContext);
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
				System.out.println("******************Init survey Services Listening*******************");
				System.out.println("******************Init survey Services Port: " + lvPort + " *************");
			} else {
				System.out.println("Cause: " + res.cause().getMessage());
				System.out.println("******************Init survey Services Start Failed*******************");
				vertx.close();
				System.exit(-1);
			}
		});
		initConfig();
	}

	private void handlerLogin(final RoutingContext pvRtx) {
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
									Session session = pvRtx.session();
									session.data().put("logindata", resp.getValue("data"));
									session.data().put(FieldName.USERNAME, messageBody.getString(FieldName.USERNAME));
									session.data().put(FieldName.USERID,
											resp.getJsonObject(FieldName.DATA).getString(FieldName._ID));
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

	private void handlerLogout(RoutingContext rtx) {
		if (rtx.session() != null) {
			rtx.session().destroy();
			rtx.session().regenerateId();
		}
		doResponseNoRenewCookie(rtx, Json.encode(MessageDefault.SessionTimeOut()));
	}

	private void handlerSurveyAction(RoutingContext pvRtx) {
		String actionName = pvRtx.pathParam("action");
		if (isDebug) {
			JsonObject messageBody = pvRtx.getBodyAsJson();
			if (pvRtx.session().get(FieldName.USERID) != null) {
				messageBody.put(FieldName.USERID, pvRtx.session().get(FieldName.USERID).toString());
			}

			messageBody.put("action", pvRtx.pathParam("action"));
			handlerAction(pvRtx, messageBody);
			return;
		}

		// check auth
		if (LISTACTIONALLOWANONYMOUS.contains(actionName)) {
			JsonObject messageBody = pvRtx.getBodyAsJson();
			messageBody.put("action", pvRtx.pathParam("action"));
			if (pvRtx.session() != null) {
				messageBody.put("logindata", (JsonObject) pvRtx.session().get("logindata"));
				if (pvRtx.session().get(FieldName.USERNAME) != null
						&& (messageBody.getValue(FieldName.USERNAME) != null)) {
					if (!pvRtx.session().get(FieldName.USERNAME).equals(messageBody.getString(FieldName.USERNAME))) {
						sendCheckAuthFail(pvRtx);
						return;
					}
				} else {
					messageBody.put(FieldName.USERNAME, "anonymous_" + pvRtx.session().id());
				}
			} else {
				messageBody.put("logindata", new JsonObject());
				messageBody.put(FieldName.USERNAME, "anonymous_" + new Date().getTime());
			}
			handlerAction(pvRtx, messageBody);
		} else {
			if (pvRtx.session() == null) {
				sendCheckAuthFail(pvRtx);
				return;
			} else if (pvRtx.session().get(FieldName.USERNAME) == null) {
				sendCheckAuthFail(pvRtx);
				return;
			}
			JsonObject messageBody = pvRtx.getBodyAsJson();
			if (!pvRtx.session().get(FieldName.USERNAME).equals(messageBody.getString(FieldName.USERNAME))) {

				sendCheckAuthFail(pvRtx);
				return;

			} else {
				if (pvRtx.session().get(FieldName.USERID) != null) {
					messageBody.put(FieldName.USERID, pvRtx.session().get(FieldName.USERID).toString());
				}
				messageBody.put(FieldName.USERID,
						pvRtx.session().get(FieldName.USERNAME).equals(messageBody.getString(FieldName._ID)));
			}
			// Check Message
			messageBody.put("action", actionName);

			handlerAction(pvRtx, messageBody);
		}

	}

	private void handlerAction(RoutingContext pvRtx, JsonObject messageBody) {
		discovery.getRecord(
				new JsonObject().put("name", EventBusDiscoveryConst.SURVEYINTERNALPROCESSORTDISCOVERY.toString()),
				resultHandler -> {
					if (resultHandler.succeeded() && resultHandler.result() != null) {
						Record record = resultHandler.result();
						mvEventBus.<JsonObject>send(record.getLocation().getString("endpoint"), messageBody,
								new DeliveryOptions().setSendTimeout(180000), res -> {
									if (res.succeeded()) {
										JsonObject resp = res.result().body();
										doResponseNoRenewCookie(pvRtx, Json.encodePrettily(resp));
									} else {
										doResponse(pvRtx, MessageDefault.RequestFailed(CodeMapping.C1111.toString(),
												res.cause().getMessage()));
									}
								});

					} else {
						doResponse(pvRtx, MessageDefault.RequestFailed("Service is unavailable"));
					}
				});

	}

	public void handlerSurveyImage(RoutingContext rtx) {
		JsonObject messageBody = rtx.getBodyAsJson();
		messageBody.put("action", rtx.pathParam("action") + "image");

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

	private void handlerGetSurvey(RoutingContext rtx) {
		if (mvWebConfig.isEmpty()) {
			initConfig();
		}

		String url = rtx.request().absoluteURI();
		JsonObject tmp = new JsonObject().put("config", mvWebConfig);
		if (rtx.session().data() != null) {
			tmp.put("logindata", (JsonObject) rtx.session().get("logindata"));
		}
		String input = rtx.pathParam("id");
		String[] id = input.split("-");
		if (id == null) {
			rtx.reroute("/");
			return;
		}
		String lvSurveyId = id[id.length - 1];

		JsonObject messageBody = new JsonObject().put(FieldName.ACTION, "retrievesurveybaseinfo")
				.put(FieldName.SURVEYID, lvSurveyId);
		discovery.getRecord(
				new JsonObject().put("name", EventBusDiscoveryConst.SURVEYINTERNALPROCESSORTDISCOVERY.toString()),
				resultHandler -> {
					if (resultHandler.succeeded() && resultHandler.result() != null) {
						Record record = resultHandler.result();
						mvEventBus.<JsonObject>send(record.getLocation().getString("endpoint"), messageBody, res -> {
							if (res.succeeded()) {
								String config = "<script>window._SURVEYCONFIG_= " + Json.encode(tmp) + "</script>";
								JsonObject resp = res.result().body();
								if (resp.getString(FieldName.CODE).equals(CodeMapping.S0000.name())) {
									String surveyTitle = resp.getJsonObject(FieldName.DATA).getString(FieldName.TITLE);
									String des = resp.getJsonObject(FieldName.DATA).getString(FieldName.DESCRIPTION);
									des = des == null ? surveyTitle : des;
									String finalDes = des;
									String image = resp.getJsonObject(FieldName.DATA).getJsonObject(FieldName.SETTING)
											.getString(FieldName.BACKGROUND_IMAGE);
									if (Webroot != null) {
										io.vertx.core.buffer.Buffer bf = Webroot;
										Buffer z;
										try {
											Document doc = Jsoup.parse(bf.toString(), "UTF-8");
											doc.head().getElementsByTag("title").html(surveyTitle);
											String ggHtml = "<meta charset=\"utf-8\">\r\n"
													+ "    <meta name=\"Description\" CONTENT=\"" + des + "\">\r\n"
													+ "    <meta name=\"google-site-verification\" content=\"UgBZWyHadggxerdmIv2ADcJv78UWnY0E9LK6YjlbkVw\"/>"
													+ "    <meta name=\"robots\" content=\"all\">";

											String fbHtml = "<meta property=\"og:url\"  content=\"" + url + "\" />\r\n"
													+ "<meta property=\"fb:app_id\"          content=\"" + fbAppID
													+ "\" /> " + "<meta property=\"og:title\"  content=\"" + surveyTitle
													+ "\" />\r\n" + "<meta property=\"og:description\" content=\"" + des
													+ "\" />\r\n" + "<meta property=\"og:image\"  content=\"" + image
													+ "\" />";
											doc.head().append(ggHtml);
											doc.head().append(fbHtml);
											doc.head().append(config);
											z = Buffer.buffer(doc.html());
										} catch (Exception e) {
											z = bf;
										}
										rtx.response().setWriteQueueMaxSize(z.length() * 2);
										rtx.response().putHeader("Content-Type", "text/html;charset=UTF-8");
										rtx.response().putHeader("Content-Length", (z.length()) + "");
										rtx.response().write(z).end();
									} else {
										vertx.fileSystem().readFile("./webroot/index.html", handler -> {
											if (handler.succeeded() && handler.result() != null) {
												Buffer z;
												io.vertx.core.buffer.Buffer bf = handler.result();

												try {
													Document doc = Jsoup.parse(bf.toString(), "UTF-8");
													doc.head().getElementsByTag("title").html(surveyTitle);
													String ggHtml = "<meta charset=\"utf-8\">\r\n"
															+ "    <meta name=\"Description\" CONTENT=\"" + finalDes
															+ "\">\r\n"
															+ "    <meta name=\"google-site-verification\" content=\"UgBZWyHadggxerdmIv2ADcJv78UWnY0E9LK6YjlbkVw\"/>"
															+ "    <meta name=\"robots\" content=\"all\">";

													String fbHtml = "<meta property=\"og:url\"  content=\"" + url
															+ "\" />\r\n"
															+ "<meta property=\"fb:app_id\"          content=\""
															+ fbAppID + "\" /> "
															+ "<meta property=\"og:title\"  content=\"" + surveyTitle
															+ "\" />\r\n"
															+ "<meta property=\"og:description\" content=\"" + finalDes
															+ "\" />\r\n" + "<meta property=\"og:image\"  content=\""
															+ image + "\" />";
													doc.head().append(ggHtml);
													doc.head().append(fbHtml);
													doc.head().append(config);
													z = Buffer.buffer(doc.html());
												} catch (Exception e) {
													z = bf;
												}
												rtx.response().setWriteQueueMaxSize(z.length() * 2);
												rtx.response().putHeader("Content-Type", "text/html;charset=UTF-8");
												rtx.response().putHeader("Content-Length", (z.length()) + "");
												rtx.response().write(z).end();
												Webroot = handler.result();
											}
										});
									}
								} else {
									responseHomeIndex(rtx);
								}
							} else {
								responseHomeIndex(rtx);
							}
						});
					} else {
						doResponse(rtx, MessageDefault.RequestFailed(resultHandler.cause().getMessage()));
					}
				});
	}

	public static void doResponse(RoutingContext rtx, JsonObject returnObj) {
		doResponse(rtx, Json.encodePrettily(returnObj));
	}

	public static void doResponse(RoutingContext rtx, String returnObj) {
		try {
			rtx.getCookie(ItradePrivateCookie).setMaxAge(ITRADEACTIONTIMEOUT / 1000);
		} catch (Exception e) {
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
		JsonObject messageBody = rtx.getBodyAsJson();
		if (isDebug) {
			if (rtx.session().get(FieldName.USERID) != null) {
				messageBody.put(FieldName.USERID, rtx.session().get(FieldName.USERID).toString());
			}
			String discoveryKey = BANKGATEWAYDISCOVERY.get(method);
			messageBody.put(FieldName.METHOD, method);
			messageBody.put("action", "payment");
			messageBody.put(FieldName.DW, action);
			messageBody.put(FieldName.DISCOVERYKEY, discoveryKey);
			discovery.getRecord(
					new JsonObject().put("name", EventBusDiscoveryConst.SURVEYINTERNALPROCESSORTDISCOVERY.toString()),
					resultHandler -> {
						if (resultHandler.succeeded() && resultHandler.result() != null) {
							Record record = resultHandler.result();
							mvEventBus.<JsonObject>send(record.getLocation().getString("endpoint"), messageBody,
									res -> {
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
		} else {
			if (rtx.session() == null) {
				sendCheckAuthFail(rtx);
				return;
			} else if (rtx.session().get(FieldName.USERNAME) == null) {
				sendCheckAuthFail(rtx);
				return;
			}
			String discoveryKey = BANKGATEWAYDISCOVERY.get(method);
			messageBody.put(FieldName.METHOD, method);
			messageBody.put("action", "payment");
			messageBody.put(FieldName.DW, action);
			messageBody.put(FieldName.DISCOVERYKEY, discoveryKey);
			discovery.getRecord(
					new JsonObject().put("name", EventBusDiscoveryConst.SURVEYINTERNALPROCESSORTDISCOVERY.toString()),
					resultHandler -> {
						if (resultHandler.succeeded() && resultHandler.result() != null) {
							Record record = resultHandler.result();
							mvEventBus.<JsonObject>send(record.getLocation().getString("endpoint"), messageBody,
									res -> {
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

	private void handlerCashConfirm(RoutingContext rtx) {
		JsonObject js = new JsonObject();
		js.put(FieldName.METHOD, rtx.pathParam(FieldName.METHOD));
		js.put(FieldName.RESULT, rtx.pathParam(FieldName.ACTION));
		js.put(FieldName.ACTION, "confirmcash");
		for (int i = 0; i < rtx.queryParams().entries().size(); i++) {
			js.put(rtx.queryParams().entries().get(i).getKey(), rtx.queryParams().entries().get(i).getValue());
		}
		discovery.getRecord(
				new JsonObject().put("name", EventBusDiscoveryConst.SURVEYINTERNALPROCESSORTDISCOVERY.toString()),
				resultHandler -> {
					if (resultHandler.succeeded() && resultHandler.result() != null) {
						Record record = resultHandler.result();
						mvEventBus.<JsonObject>send(record.getLocation().getString("endpoint"), js, res -> {
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
		} else if (step.equals("2")) {
			rtx.reroute("/confirmresetpassword.html");
			return;
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
						VertxServiceCenter.getEventbus().send(record.getLocation().getString("endpoint"), lvMessage,
								res -> {
									if (res.succeeded()) {
										doResponse(rtx, (JsonObject) res.result().body());
									} else {

										doResponse(rtx,
												MessageDefault.RequestFailed(resultHandler.cause().getMessage()));

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
								resp.remove(FieldName.DATA);
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
							VertxServiceCenter.getEventbus()
									.<JsonObject>send(record.getLocation().getString("endpoint"), message, res -> {
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

	private void redirectTo(RoutingContext rtx, String url) {
		String host = new String(rtx.request().host().toString());
		Boolean isSSL = rtx.request().isSSL();

		String uri = (isSSL ? "https" : "http") + "://" + host + url;
		String html = new String("<script>window.location = \"" + uri + "\"</script>");
		Buffer buffer = Buffer.buffer(html);
		rtx.response().setChunked(true).putHeader("Content-Type", "text/html").setStatusCode(200).write(buffer).end();
	}

	private void sendCheckAuthFail(RoutingContext rtx) {
		doResponseNoRenewCookie(rtx, Json.encode(MessageDefault.SessionTimeOut()));
	}

	private void responseHomeIndex(RoutingContext rtx) {
		if (mvWebConfig.isEmpty()) {
			initConfig();
		}
		JsonObject tmp = new JsonObject().put("config", mvWebConfig);
		tmp.put("logindata", (JsonObject) rtx.session().get("logindata"));

		vertx.fileSystem().readFile("./webroot/index.html", handler -> {
			if (handler.succeeded() && handler.result() != null) {

				Buffer z;
				String x = "<script>window._SURVEYCONFIG_= " + Json.encode(tmp) + "</script>";
				io.vertx.core.buffer.Buffer bf = handler.result();

				try {
					Document doc = Jsoup.parse(bf.toString(), "UTF-8");
					doc.head().append(x);
					z = Buffer.buffer(doc.html());
				} catch (Exception e) {
					z = bf;
				}
				rtx.response().setWriteQueueMaxSize(z.length() * 2);
				rtx.response().putHeader("Content-Type", "text/html;charset=UTF-8");
				rtx.response().putHeader("Content-Length", (z.length()) + "");
				rtx.response().write(z).end();
				Webroot = handler.result();
			}
		});
	}

	private void handlerExploer(RoutingContext rtx) {
		if (mvWebConfig.isEmpty()) {
			initConfig();
		}
		JsonObject tmp = new JsonObject().put("config", mvWebConfig);

		vertx.fileSystem().readFile("./webroot/index.html", handler -> {
			if (handler.succeeded() && handler.result() != null) {

				Buffer z;
				String x = "<script>window._SURVEYCONFIG_= " + Json.encode(tmp) + "</script>";
				io.vertx.core.buffer.Buffer bf = handler.result();
				String surveyTitle = "Khám phá khảo sát tại Survey Go";
				try {
					Document doc = Jsoup.parse(bf.toString(), "UTF-8");
					doc.head().append(x);
					doc.head().getElementsByTag("title").html(surveyTitle);
					String ggHtml = "<meta charset=\"utf-8\">\r\n" + "    <meta name=\"Description\" CONTENT=\""
							+ surveyTitle + "\">\r\n"
							+ "    <meta name=\"google-site-verification\" content=\"UgBZWyHadggxerdmIv2ADcJv78UWnY0E9LK6YjlbkVw\"/>"
							+ "    <meta name=\"robots\" content=\"all\">";

					String fbHtml = "<meta property=\"og:url\"  content=\"" + rtx.request().absoluteURI() + "\" />\r\n"
							+ "<meta property=\"fb:app_id\"          content=\"" + fbAppID + "\" /> "
							+ "<meta property=\"og:title\"  content=\"" + surveyTitle + "\" />\r\n";

					doc.head().append(ggHtml);
					doc.head().append(fbHtml);

					z = Buffer.buffer(doc.html());
				} catch (Exception e) {
					z = bf;
				}
				rtx.response().setWriteQueueMaxSize(z.length() * 2);
				rtx.response().putHeader("Content-Type", "text/html;charset=UTF-8");
				rtx.response().putHeader("Content-Length", (z.length()) + "");
				rtx.response().write(z).end();
				Webroot = handler.result();
			}
		});
	}

	private void initConfig() {
		discovery.getRecord(
				new JsonObject().put("name", EventBusDiscoveryConst.SURVEYINTERNALPROCESSORTDISCOVERY.toString()),
				resultHandler -> {
					if (resultHandler.succeeded() && resultHandler.result() != null) {
						Record record = resultHandler.result();
						JsonObject messageBody = new JsonObject().put(FieldName.ACTION, "retrieveconfig")
								.put(FieldName.METHOD, "");
						mvEventBus.<JsonObject>send(record.getLocation().getString("endpoint"), messageBody, res -> {
							if (res.succeeded()) {
								JsonObject resp = res.result().body();
								mvWebConfig = resp.getJsonObject(FieldName.DATA);
							}
						});

					}
				});
	}

}
