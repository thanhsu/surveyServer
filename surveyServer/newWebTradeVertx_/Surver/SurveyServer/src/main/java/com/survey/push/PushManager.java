package com.survey.push;

import org.atmosphere.client.TrackMessageSizeInterceptor;
import org.atmosphere.config.service.*;
import org.atmosphere.config.service.DeliverTo.DELIVER_TO;
import org.atmosphere.cpr.*;
import org.atmosphere.interceptor.*;
import com.hazelcast.com.eclipsesource.json.JsonObject;
import com.survey.constant.AtmosphereAPI;
import com.survey.constant.PushResponseJSONBean;
import com.survey.utils.Log;

import atmosphere.decoder.ProtocolDecoder;
import atmosphere.encoder.JacksonEncoder;
import io.vertx.core.Future;
import io.vertx.core.cli.annotations.Option;
import java.io.IOException;
import javax.inject.Inject;

@ManagedService(path = "/push/{module}/{action: [a-zA-Z]+}/{session: [a-zA-Z0-9]+}",
    interceptors = {AtmosphereResourceLifecycleInterceptor.class, TrackMessageSizeInterceptor.class, SuspendTrackerInterceptor.class,
        AtmosphereResourceStateRecovery.class, HeartbeatInterceptor.class, OnDisconnectInterceptor.class, IdleResourceInterceptor.class})
public class PushManager {
  @Inject
  private BroadcasterFactory factory;


  @Inject
  private AtmosphereResourceFactory resourceFactory;

  @Inject
  private MetaBroadcaster metaBroadcaster;

  @PathParam("module")
  private String mvModule;


  @PathParam("session")
  private String mvClientID;

  @PathParam("action")
  private String mvAction;

  @Ready(encoders = {JacksonEncoder.class})
  @DeliverTo(DELIVER_TO.RESOURCE)
  public Object onReady(final AtmosphereResource resource) {
    Log.print("Browser:" + resource.uuid() + " connected!", Log.DEBUG_LOG);
    final PushResponseJSONBean lvResBean = new PushResponseJSONBean("1010", 1, null);

    resource.getResponse().addHeader("Access-Control-Allow-Origin", "*");
    AtmosphereAPI lvAPI = AtmosphereAPI.getInstance();
    lvAPI.setBroadcasterFactory(factory).setMetaBroadcaster(metaBroadcaster).setResourceFactory(resourceFactory);
    String pTopics = resource.getRequest().getRequestURI();
    // ITradeAtmosphereAPI.getInstance().subscribeTopic(resource, mvClientID, pTopics);
    Future<String> lvFuture = AtmosphereAPI.getInstance().subscribeTopic(resource, mvClientID, pTopics);
    lvFuture.setHandler(handler -> {
      if (handler.succeeded()) {
        // System.out.println("Subcribe Topic Success");
      } else {
        Log.print("Subcribe Topic Failed: " + handler.cause().getMessage());
      }
    });
    return lvResBean;
  }

  @Message(encoders = {JacksonEncoder.class}, decoders = {ProtocolDecoder.class})
  public PushResponseJSONBean onTopicSubscribe(AtmosphereResource resource) {
    final PushResponseJSONBean lvResBean = new PushResponseJSONBean("1010", 1, null);
    Log.print("Browser:" + resource.uuid() + " connected!", Log.DEBUG_LOG);
    AtmosphereAPI lvAPI = AtmosphereAPI.getInstance();
    lvAPI.setBroadcasterFactory(factory).setMetaBroadcaster(metaBroadcaster).setResourceFactory(resourceFactory);
    String pTopics = resource.getRequest().getRequestURI();
    Future<String> lvFuture = AtmosphereAPI.getInstance().subscribeTopic(resource, mvClientID, pTopics);
    lvFuture.setHandler(handler -> {
      if (handler.succeeded()) {
        // System.out.println("Subcribe Topic Success");
      } else {
        Log.print("Subcribe Topic Failed: " + handler.cause().getMessage());
      }
    });
    return lvResBean;
  }

  @Message(encoders = {JacksonEncoder.class}, decoders = {ProtocolDecoder.class})
  public PushResponseJSONBean onBroadcastMessage(PushResponseJSONBean respBean) {
    return respBean;
  }



  @Resume
  public void onResumed(AtmosphereResource resource) {
    resource.getResponse().setCharacterEncoding("UTF-8");
  }

  @Get
  public void setMessageEncoding(AtmosphereResource resource) {
    resource.getResponse().setCharacterEncoding("UTF-8");

  }

  @Post
  public void postEncoding(AtmosphereResource resource) {
    resource.getResponse().setCharacterEncoding("UTF-8");
    // if (resource.getRequest().getHeader(HeaderConfig.X_ATMOSPHERE_TRANSPORT) != null) {
    // resource.getResponse().write("xxxx");
    // }
  }

  @Option
  public void onOption(AtmosphereResource resource) {
    resource.getResponse().setCharacterEncoding("UTF-8");

  }

  @Disconnect
  public JsonObject onDisconnect(AtmosphereResourceEvent event) {
    System.out.println("On DisConnect");
    JsonObject lvRes = new JsonObject();
    try {
      String transport = event.getResource().getRequest().getHeader(HeaderConfig.X_ATMOSPHERE_TRANSPORT);
      if (transport != null && org.atmosphere.util.Utils.resumableTransport(event.getResource().transport())
          && transport.equalsIgnoreCase(HeaderConfig.DISCONNECT_TRANSPORT_MESSAGE)) {
        // System.out.println("Browser closed the connection!");
      } else {
        lvRes.add("isResumed", true);
        // System.out.println("Long-Polling Connection Resumed.");
      }

      String uuid = event.getResource().uuid();
      if (event.isCancelled()) {
        Log.println("Browser " + uuid + " unexpectedly disconnected.", Log.DEBUG_LOG);
      } else if (event.isClosedByClient()) {
        Log.println(
            "Browser " + uuid + "@Suspend( contentType = MediaType.APPLICATION_JSON, period = MAX_SUSPEND_MSEC ) closed the connection.",
            Log.DEBUG_LOG);
        lvRes.add("isResumed", false);
      } else if (event.isClosedByApplication()) {
        lvRes.add("isResumed", false);
        Log.println("Application server closed the connection: " + event.getResource().getBroadcaster().getID(), Log.ALERT_LOG);
      }
    } catch (Exception e) {
      lvRes.add("isResumed", false);
      e.printStackTrace();
      event.throwable();
    }
    try {
      event.getResource().getResponse().write("Good Bye").close();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    // event.getResource().write("Hello word");
    return lvRes;
  }

}
