package com.survey;

import java.util.HashMap;

import com.survey.dbservices.action.BaseAdminServiceAction;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.StaticHandler;

public class AdminServer extends AbstractVerticle {
	private HttpServer mvHttpServer;
	private HashMap<String, BaseAdminServiceAction> mvAdminAction = new HashMap<>();
	private StaticHandler mvStaticHandler;

	private int port;

	@Override
	public void init(Vertx vertx, Context context) {
		super.init(vertx, context);
		initAdminAction();
		port = config().getInteger("AdminServerPort");
	}

	@Override
	public void start(Future<Void> startFuture) throws Exception {
		super.start(startFuture);
		HttpServerOptions httpOption = new HttpServerOptions();
		Router router = Router.router(this.vertx);

		router.route("/admin/:action").handler(this::handlerAdminAction);

		router.route().handler(RoutingContext -> {
			if (this.mvStaticHandler == null) {
				this.mvStaticHandler = StaticHandler.create("webroot");
				this.mvStaticHandler.setCachingEnabled(false);
				this.mvStaticHandler.setIndexPage("home.html").handle(RoutingContext);
			} else {
				this.mvStaticHandler.handle(RoutingContext);
			}
		});

		mvHttpServer = vertx.createHttpServer();
		mvHttpServer.requestHandler(router::accept).listen(port, res -> {
			if (res.succeeded()) {
				System.out.println("******************Init ADMIN Services Listening*******************");
				System.out.println("******************Init ADMIN Services Port: " + port + " *************");
			} else {
				System.out.println("Cause: " + res.cause().getMessage());
				System.out.println("******************Init ADMIN Services Start Failed*******************");
				vertx.close();
				System.exit(-1);
			}
		});
	}

	private void handlerAdminAction(RoutingContext rtx) {

	}

	private void initAdminAction() {

	}
}
