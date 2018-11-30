package com.survey.etheproxy;

import java.net.Socket;
import java.nio.charset.Charset;

import com.survey.constant.EventBusDiscoveryConst;
import com.survey.utils.CodeMapping;
import com.survey.utils.FieldName;
import com.survey.utils.Log;
import com.survey.utils.MessageDefault;
import com.survey.utils.VertxServiceCenter;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpClient;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.NetSocket;
import io.vertx.servicediscovery.Record;

public class EtheSocketClient extends AbstractVerticle {
	private String host;
	private int port;

	@Override
	public void init(Vertx vertx, Context context) {
		super.init(vertx, context);
		host = config().getString("EtheSocketHost");
		port = config().getInteger("EtheSocketPort");
	}

	@Override
	public void start(Future<Void> startFuture) throws Exception {
		super.start(startFuture);
		System.out.println("*************************************************");
		System.out.println(String.format("Init socket connection to %s port %d", host, port));
		HttpClient client = vertx.createHttpClient();

		client.websocket(port, host, "/", websocket -> {
			websocket.handler(data -> {
				System.out.println("Received data " + data.toString());
				Log.println("Socket Received: " + data.toString(), Log.ACCESS_LOG);

				 JsonObject message = null;
				try {
					message = data.toJsonObject();
				} catch (Exception e) {
					message = null;
				}
				if (message != null) {
					VertxServiceCenter.getInstance().getDiscovery().getRecord(
							new JsonObject().put("name",
									EventBusDiscoveryConst.SURVEYCONFIRMPROCESSORDISCOVERY.toString()),
							resultHandler -> {
								if (resultHandler.succeeded() && resultHandler.result() != null) {
									Record record = resultHandler.result();
									VertxServiceCenter.getEventbus().<JsonObject>send(
											record.getLocation().getString("endpoint"), data.toJsonObject(), res -> {
												if (res.succeeded()) {
													if (res.result().body().getString(FieldName.CODE)
															.equals(CodeMapping.C0000.toString())) {
													}
												}
											});
								}
							});
				}
				// client.close();
			});
			websocket.write(new JsonObject().put("clientid", "web_server").toBuffer());
		});

		/*
		 * vertx.createNetClient().connect(port, host, new
		 * Handler<AsyncResult<NetSocket>>() {
		 * 
		 * @Override public void handle(AsyncResult<NetSocket> socket) { if
		 * (socket.succeeded()) { System.out.println("Socket server connected!");
		 * NetSocket netSocket = socket.result(); netSocket.handler(new
		 * Handler<Buffer>() {
		 * 
		 * @Override public void handle(Buffer arg0) { Log.println("Socket Received: " +
		 * arg0.toString(Charset.forName("UTF-8")), Log.ACCESS_LOG); JsonObject message
		 * = arg0.toJsonObject();
		 * VertxServiceCenter.getInstance().getDiscovery().getRecord( new
		 * JsonObject().put("name",
		 * EventBusDiscoveryConst.SURVEYCONFIRMPROCESSORDISCOVERY.toString()),
		 * resultHandler -> { if (resultHandler.succeeded() && resultHandler.result() !=
		 * null) { Record record = resultHandler.result();
		 * VertxServiceCenter.getEventbus().<JsonObject>send(
		 * record.getLocation().getString("endpoint"), message, res -> { if
		 * (res.succeeded()) { if (res.result().body().getString(FieldName.CODE)
		 * .equals(CodeMapping.C0000.toString())) { } } }); } }); } }); } else {
		 * System.out.println("Socket server connection fail. Cause: " +
		 * socket.cause().getMessage()); } } });
		 */
	}
}
