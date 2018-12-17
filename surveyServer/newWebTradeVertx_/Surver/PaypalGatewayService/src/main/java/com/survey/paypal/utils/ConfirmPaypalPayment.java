package com.survey.paypal.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.ParseException;
import java.util.HashMap;

import com.survey.constant.EventBusDiscoveryConst;
import com.survey.paypal.PaypalConnection;
import com.survey.utils.FieldName;
import com.survey.utils.Log;
import com.survey.utils.VertxServiceCenter;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.WebClient;

public class ConfirmPaypalPayment {
	private static HashMap<String, String> tranIDMappingLinkInfo = new HashMap<>();
	public static String token = "Bearer A21AAHXGZmDs5droz3MPOv2aM_XodmsZrGKCBd5lQVeuDnE9hOjBpiOKVBOGcSKbe1xTRanNEqVyckOgOLMAOUYvbX7cNkk3Q";
	static {
		loadFromFile();
		startChecking();
	}
	private static final String cacheFile = "./store/data.dat";

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
					storeLinkToFile();
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
						if (!item.getString("transaction_status").equals("PENDING")) {

							JsonObject messageConfirm = new JsonObject().put(FieldName.ACTION, "paypalconfirm")
									.put(FieldName.TRANID, tranID.getKey()).put(FieldName.DATA, item);
							VertxServiceCenter.getInstance().getEventbus().send(
									EventBusDiscoveryConst.SURVEYCONFIRMPROCESSORDISCOVERY.value(), messageConfirm);
							tranIDMappingLinkInfo.remove(tranID.getKey());
						}
					}
				}
			});

		});
	}

	private static void loadFromFile() {
		File f = new File(ConfirmPaypalPayment.cacheFile);
		FileReader fr = null;
		BufferedReader br = null;
		try {
			if (f.exists()) {
				fr = new FileReader(f);
				br = new BufferedReader(fr);
				String textRead = br.readLine();
				while (textRead != null) {
					String[] tmp = textRead.split("=");
					if (tmp.length > 1) {
						tranIDMappingLinkInfo.put(tmp[0], tmp[1]);
					}
					textRead = br.readLine();
				}
			}

		} catch (IOException e) {

		} finally {
			try {
				if (fr != null) {
					fr.close();
				}
				if (br != null) {
					br.close();
				}
			} catch (IOException e) {

			}
		}
	}

	private static void storeLinkToFile() {
		if (tranIDMappingLinkInfo.isEmpty()) {
			return;
		}
		String data = "";
		for (String key : tranIDMappingLinkInfo.keySet()) {
			data = data + key + "=" + tranIDMappingLinkInfo.get(key) + "\n";
		}
		if (data.equals("")) {
			return;
		}
		try {
			File lvFile = new File(cacheFile);
			if (!lvFile.exists()) {
				lvFile.createNewFile();
			}
			Files.write(Paths.get(cacheFile), (data).getBytes(), StandardOpenOption.TRUNCATE_EXISTING);
		} catch (Exception ex) {
			ex.printStackTrace();
			Log.print("Can not caching to file. Cause: " + ex.getMessage(), Log.ERROR_LOG);
		}
	}

}
