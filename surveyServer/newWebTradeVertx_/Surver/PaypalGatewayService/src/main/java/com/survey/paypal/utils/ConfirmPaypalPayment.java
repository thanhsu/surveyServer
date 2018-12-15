package com.survey.paypal.utils;

import java.util.HashMap;

import com.survey.constant.EventBusDiscoveryConst;
import com.survey.paypal.PaypalConnection;
import com.survey.utils.FieldName;
import com.survey.utils.VertxServiceCenter;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.WebClient;

public class ConfirmPaypalPayment {
	private static HashMap<String, String> tranIDMappingLinkInfo = new HashMap<>();
	public static String token = "Bearer A21AAHXGZmDs5droz3MPOv2aM_XodmsZrGKCBd5lQVeuDnE9hOjBpiOKVBOGcSKbe1xTRanNEqVyckOgOLMAOUYvbX7cNkk3Q";
	static {
		startChecking();
	}

	public static void addTranMappingLink(String tranID, JsonObject paypalResponse) {
		JsonObject batch = paypalResponse.getJsonObject("batch_header");
		if (batch.getString("batch_status").equalsIgnoreCase("PENDING")) {
			paypalResponse.getJsonArray("links").forEach(json -> {
				String link = ((JsonObject) json).getString("href");
				if (link == null) {
					return;
				}
				tranIDMappingLinkInfo.put(tranID, link);
			});
		}
	}

	private static void startChecking() {
		Thread x = new Thread() {
			@Override
			public void run() {
				while (true) {
					checkAllPendingRequest();
					try {
						Thread.sleep(10000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

			}
		};
		x.start();
	}

	private static void checkAllPendingRequest() {
		if (tranIDMappingLinkInfo.isEmpty()) {
			return;
		}
		@SuppressWarnings("static-access")
		WebClient webClient = WebClient.create(VertxServiceCenter.getInstance().getVertx());
		tranIDMappingLinkInfo.entrySet().forEach(tranID -> {
			HttpRequest<Buffer> httpRequest;
			httpRequest = webClient.getAbs(tranID.getValue());
			httpRequest.putHeader("Content-Type", "application/json");
			httpRequest.putHeader("Authorization", token);
			httpRequest.send(h -> {
				if (h.result() != null) {
					JsonObject message = h.result().bodyAsJsonObject();

					JsonObject batStatus = message.getJsonObject("batch_header");
					System.out.println("Check status trans: " + h.result().bodyAsString());
					if (!batStatus.getString("batch_status").equalsIgnoreCase("PROCESSING")) {
						JsonObject item = message.getJsonArray("items").getJsonObject(0);
						
						JsonObject messageConfirm = new JsonObject().put(FieldName.ACTION, "paypalconfirm")
								.put(FieldName.TRANID, tranID.getKey()).put(FieldName.DATA, item);
						VertxServiceCenter.getInstance().getEventbus()
								.send(EventBusDiscoveryConst.SURVEYCONFIRMPROCESSORDISCOVERY.value(), messageConfirm);
						tranIDMappingLinkInfo.remove(tranID.getKey());
					} 
				}
			});

		});
	}

}
