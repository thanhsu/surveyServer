package com.survey.utils.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.impl.ConcurrentHashSet;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.ServiceDiscoveryOptions;
import io.vertx.servicediscovery.types.EventBusService;
import io.vertx.servicediscovery.types.HttpEndpoint;
import io.vertx.servicediscovery.types.MessageSource;

public class MicroServiceVerticle extends AbstractVerticle {
  protected ServiceDiscovery discovery;
  protected Set<Record> registeredRecords = new ConcurrentHashSet<>();
  private static String ADDRESS = "serviceannounce";
  private static String NAME = "myname";

  @Override
  public void init(Vertx vertx, Context context) {
    super.init(vertx, context);
    try {
      ADDRESS = config().getString("discoveryaddress");
      NAME = config().getString("discoveryname");
    } catch (Exception e) {
    	
    }

  }

  @Override
  public void start(Future<Void> startFuture) throws Exception {
    discovery = ServiceDiscovery.create(vertx, new ServiceDiscoveryOptions().setAnnounceAddress(ADDRESS).setName(NAME));
    startFuture.complete();
  }

  protected void pushlish(Record record, Handler<AsyncResult<Void>> completionHandler) {
    if (discovery == null) {
      try {
        discovery = ServiceDiscovery.create(vertx, new ServiceDiscoveryOptions().setAnnounceAddress("service-announce").setName("my-name"));

      } catch (Exception e) {
        throw new RuntimeException("Cannot create discovery service");
      }
    }

    discovery.publish(record, rs -> {
      if (rs.succeeded()) {
        registeredRecords.add(record);
      }
      completionHandler.handle(rs.map((Void) null));
    });
  }

  public void publishHttpEndpoint(String name, String host, int port, Handler<AsyncResult<Void>> completionHandler) {
    Record record = HttpEndpoint.createRecord(name, host, port, "/");
    pushlish(record, completionHandler);
  }

  public void publishMessageSource(String name, String address, Class<?> contentClass, Handler<AsyncResult<Void>> completionHandler) {
    Record record = MessageSource.createRecord(name, address, contentClass);
    pushlish(record, completionHandler);
  }

  public void publishMessageSource(String name, String address, Handler<AsyncResult<Void>> completionHandler) {
    Record record = MessageSource.createRecord(name, address);
    pushlish(record, completionHandler);
  }

  public void publishEventBusService(String name, String address, Class<?> contentClass, Handler<AsyncResult<Void>> completionHandler) {
    Record record = EventBusService.createRecord(name, address, contentClass);
    // record.setType("eventbus-service-proxy");
    pushlish(record, completionHandler);
  }

  public void publishEventBusService(String name, String address, Handler<AsyncResult<Void>> completionHandler) {
    Record record = new Record().setName(name).setType("eventbus-service-proxy").setLocation(new JsonObject().put("endpoint", address))
    /* .setMetadata(new JsonObject().put("test","test")) */;
    // record.setType("eventbus-service-proxy");
    pushlish(record, completionHandler);
  }

  @Override
  public void stop(Future<Void> future) throws Exception {
    @SuppressWarnings({"unchecked", "rawtypes"})
    List<Future> futures = new ArrayList();
    for (Record record : registeredRecords) {
      Future<Void> unregistrationFuture = Future.future();
      futures.add(unregistrationFuture);
      discovery.unpublish(record.getRegistration(), unregistrationFuture);
    }

    if (futures.isEmpty()) {
      discovery.close();
      future.complete();
    } else {
      CompositeFuture composite = CompositeFuture.all(futures);
      composite.setHandler(rs -> {
        discovery.close();
        if (rs.failed()) {
          future.fail(rs.cause());
        } else {
          future.complete();
        }
      });
    }
  }

}
