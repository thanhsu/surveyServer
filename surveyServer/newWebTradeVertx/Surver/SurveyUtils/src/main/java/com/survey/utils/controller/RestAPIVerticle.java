package com.survey.utils.controller;

import java.util.HashSet;
import java.util.Set;
import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.CookieHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.sstore.ClusteredSessionStore;
import io.vertx.ext.web.sstore.LocalSessionStore;

public abstract class RestAPIVerticle extends MicroServiceVerticle {

  @Override
  public void init(Vertx vertx, Context context) {
    super.init(vertx, context);
  }

  @Override
  public void start(Future<Void> future) throws Exception {
    super.start(future);
  }

  protected Future<Void> createHttpServer(Router router, String host, int port) {
    Future<HttpServer> httpServerFuture = Future.future();
    vertx.createHttpServer().requestHandler(router::accept).listen(port, host, httpServerFuture.completer());
    return httpServerFuture.map(r -> null);
  }

  protected void enableCorsSupport(Router router) {
    Set<String> allowsHeaders = new HashSet<>();
    allowsHeaders.add("x-requested-with");
    allowsHeaders.add("Access-Control-Allow-Origin");
    allowsHeaders.add("origin");
    allowsHeaders.add("Content-Type");
    allowsHeaders.add("accept");
    Set<HttpMethod> allowsMethods = new HashSet<>();
    allowsMethods.add(HttpMethod.GET);
    allowsMethods.add(HttpMethod.POST);
    allowsMethods.add(HttpMethod.PUT);
    allowsMethods.add(HttpMethod.OPTIONS);
    allowsMethods.add(HttpMethod.DELETE);
    allowsMethods.add(HttpMethod.PATCH);

    router.route().handler(CorsHandler.create("*").allowedHeaders(allowsHeaders).allowedMethods(allowsMethods));
  }

  protected void enableLocalSession(Router router) {
    router.route().handler(CookieHandler.create());
    router.route().handler(SessionHandler.create(LocalSessionStore.create(vertx, "wtrade.user.session")));
  }

  protected void enableClusterSession(Router router) {
    router.route().handler(CookieHandler.create());
    router.route().handler(SessionHandler.create(ClusteredSessionStore.create(vertx, "wtrade.user.session")));
  }

  // Handler Result
  protected <T> Handler<AsyncResult<T>> resultHander(RoutingContext ctx, Handler<T> handler) {
    return res -> {
      if (res.succeeded()) {
        handler.handle(res.result());
      } else {
        internalError(ctx, res.cause());
        res.cause().printStackTrace();
      }
    };
  }

  // use the result directly and invoke 'toString' as the response
  protected <T> Handler<AsyncResult<T>> resultHandler(RoutingContext ctx) {
    return rs -> {
      if (rs.succeeded()) {
        T res = rs.result();
        ctx.response().putHeader("content-type", "application/json;charset=utf-8").end(res == null ? "" : res.toString());
      } else {
        internalError(ctx, rs.cause());
        rs.cause().printStackTrace();
      }
    };
  }

  /**
   * The result is not need. Only the state of tje asunc result is required
   * 
   */
  protected Handler<AsyncResult<Void>> resultVoidHandler(RoutingContext ctx, JsonObject result, int status) {
    return res -> {
      if (res.succeeded()) {
        ctx.response().setStatusCode(status == 0 ? 200 : status).putHeader("content-type", "application/json").end(result.encodePrettily());
      } else {
        internalError(ctx, res.cause());
        res.cause().printStackTrace();
      }
    };
  }

  protected Handler<AsyncResult<Void>> resultVoidHandler(RoutingContext ctx, JsonObject result) {
    return resultVoidHandler(ctx, result, 200);
  }

  // create helper method

  protected void badRequest(RoutingContext context, Throwable e) {
    context.response().setStatusCode(400).putHeader("content-type", "application/json;charset=utf-8")
        .end(new JsonObject().put("error", e.getMessage()).encodePrettily());
  }

  protected void notFound(RoutingContext context) {
    context.response().setStatusCode(404).putHeader("content-type", "application/json;charset=utf-8")
        .end(new JsonObject().put("message", "not found").encodePrettily());
  }

  protected void internalError(RoutingContext context, Throwable e) {
    context.response().setStatusCode(500).putHeader("content-type", "application/json;charset=utf-8")
        .end(new JsonObject().put("error", e.getMessage()).encodePrettily());
  }

  protected void notImplemented(RoutingContext context) {
    context.response().setStatusCode(501).putHeader("content-type", "application/json;charset=utf-8")
        .end(new JsonObject().put("error", "not Implemented").encodePrettily());
  }

  protected void badGateway(RoutingContext context, Throwable e) {
    e.printStackTrace();
    context.response().setStatusCode(502).putHeader("content-type", "application/json;charset=utf-8")
        .end(new JsonObject().put("error", e.getMessage()).encodePrettily());
  }

  protected void serviceUnavailable(RoutingContext context, String cause) {
    context.response().setStatusCode(500).putHeader("content-type", "application/json;charset=utf-8")
        .end(new JsonObject().put("error", cause).encodePrettily());
  }

}
