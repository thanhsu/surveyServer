package com.survey.etheproxy;

import io.vertx.core.http.WebSocket;
import io.vertx.core.json.JsonObject;

public class Reconnect {
	public static WebSocket mvWebSocket = null;
	public static String clientID = "";
	public static long interval = 30000;

	public void reConnect() {
		Thread t = new Thread() {
			@Override
			public void run() {
				while (true) {
					if (mvWebSocket != null) {
						mvWebSocket.write(new JsonObject().put("clientid", clientID).toBuffer());
					}
					long i = interval - 5000;
					while (i > 0) {
						try {
							Thread.sleep(5000);

						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						i = i - 5000;
					}
				}
			}
		};
		t.start();
	}
}
