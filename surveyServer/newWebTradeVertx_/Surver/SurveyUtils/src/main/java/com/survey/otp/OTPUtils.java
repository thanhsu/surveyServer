package com.survey.otp;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Scanner;

import com.survey.utils.Log;

import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class OTPUtils {
	private static HashMap<String, String> mvMessageConfig = new HashMap<>();
	private static OTPUtils instance = null;
	private static final String mvConfigPath = "./config/messagenotification.json";

	public void init() {
		File lvFile = new File(mvConfigPath);
		if (!lvFile.exists()) {
			Log.print("Init OTP Message Error", Log.ERROR_LOG);
		} else {
			JsonObject lvJsonObject = getConfiguration(lvFile);
			JsonArray lvListMessage = lvJsonObject.getJsonArray("MessageKey");
			JsonArray lvListLanguage = lvJsonObject.getJsonArray("Language");
			lvListMessage.forEach(m -> {
				String lvKey = m.toString();
				lvListLanguage.forEach(l -> {
					String lvLang = l.toString();
					String lvTemp = lvJsonObject.getString(lvKey + "_" + lvLang);
					mvMessageConfig.put(lvKey + "_" + lvLang, lvTemp);
				});
			});
		}
	}

	public static OTPUtils getInstance() {
		if (instance == null) {
			synchronized (OTPUtils.class) {
				instance = new OTPUtils();
				instance.init();
			}
		}
		return instance;
	}

	public String getMessage(String pvKey, String pvLang) {
		return mvMessageConfig.get(pvKey + "_" + (pvLang == null ? "en_US" : pvLang));
	}

	public String generateMessage(String pvKey, String pvLang, Hashtable<String, String> pvParams) {
		String lvTmp = getMessage(pvKey, pvLang);
		String[] lvKey = pvParams.keySet().toArray(new String[0]);
		for (int i = 0; i < lvKey.length; i++) {
			lvTmp = lvTmp.replaceAll(lvKey[i], pvParams.get(lvKey[i]));
		}
		return lvTmp;
	}

	public String generateMessage(String pvKey, String pvLang, JsonObject pvParams) {
		String lvTmp = getMessage(pvKey, pvLang);
		String[] lvKey = pvParams.fieldNames().toArray(new String[0]);
		for (int i = 0; i < lvKey.length; i++) {
			try {
				lvTmp = lvTmp.replaceAll(lvKey[i], pvParams.getString(lvKey[i]));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return lvTmp;
	}

	private JsonObject getConfiguration(File config) {
		JsonObject conf = new JsonObject();
		if (config.isFile()) {
			System.out.println("Reading Json Message OTP config file: " + config.getAbsolutePath());
			try (Scanner scanner = new Scanner(config).useDelimiter("\\A")) {
				String sconf = scanner.next();
				try {
					conf = new JsonObject(sconf);
				} catch (DecodeException e) {
					e.printStackTrace();
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		} else {
			System.out.println("Config file not found: " + config.getAbsolutePath());
		}
		return conf;
	}

	public static void main(String[] args) {
		OTPUtils.getInstance();
		Hashtable<String, String> lvtest = new Hashtable<>();
		lvtest.put("#counterpartyac", "123213213");
		lvtest.put("#otp", "O123");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		long lvDateExpired = new Date().getTime() + 300000;
		lvtest.put("#requesttime", sdf.format(new Date(lvDateExpired)));

		System.out.println("test: " + OTPUtils.getInstance().generateMessage("CashDW", "en_US", lvtest));

	}
}
