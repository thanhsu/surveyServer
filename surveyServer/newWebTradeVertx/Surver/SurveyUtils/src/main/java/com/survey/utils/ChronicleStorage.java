package com.survey.utils;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.ConcurrentMap;

import com.survey.otp.OTPUser;

import net.openhft.chronicle.map.ChronicleMapBuilder;

public class ChronicleStorage {
	public static final String CLI_SEQ = "requestSeq";

	public static ConcurrentMap<String, Long> createSequenceStorage(String filepath) throws IOException {
		File file = new File(filepath);
		ChronicleMapBuilder<String, Long> builder = ChronicleMapBuilder.of(String.class, Long.class).entries(10);
		builder.averageKey("requestSeq");
		return builder.createPersistedTo(file);
	}

	public static ConcurrentMap<String, OTPUser> createOTPStorage(String filePath, String key) {
		File file = new File(filePath);
		ChronicleMapBuilder<String, OTPUser> builder = ChronicleMapBuilder.of(String.class, OTPUser.class);
		builder.averageKey(key);
		builder.averageValue(new OTPUser("1232153213213", "1232154123125324213125324123123", new Date(),
				"1231131231232513123123123123123123123", "131231231235ioujahdkjasgdkg9128310273", 5));
		try {
			builder.entries(10000000);
			return builder.createPersistedTo(file);
		} catch (IOException e) {
			Log.print("Init ChronicleStorage failed. Cause: " + e.getMessage());
			e.printStackTrace();
			return null;
		}
	}

	public static ConcurrentMap<String, HashMap> createOrderListCache(String filePath, String key) {
		File file = new File(filePath);
		ChronicleMapBuilder<String, HashMap> builder = ChronicleMapBuilder.of(String.class, HashMap.class);
		builder.averageKey(key);
		HashMap<String, HashMap<String, String>> lvTmp = new HashMap<>();

		builder.averageValue(lvTmp);
		try {
			builder.entries(10000000);
			return builder.createPersistedTo(file);
		} catch (IOException e) {
			Log.print("Init ChronicleStorage failed. Cause: " + e.getMessage());
			e.printStackTrace();
			return null;
		}
	}

	/*
	 * public static ConcurrentMap<String, OTPUser> getOTPStorage(String key) { File
	 * file = new File(filePath); ChronicleMapBuilder<String, OTPUser> builder =
	 * ChronicleMapBuilder.of(String.class, OTPUser.class); builder.averageKey(key);
	 * try { return builder.createPersistedTo(file); } catch (IOException e) {
	 * e.printStackTrace(); return null; } }
	 */

	static Long requestSeq = null;

	public long getSequenceMessage(ConcurrentMap<String, Long> seqStorage) {
		if (requestSeq == null) {
			requestSeq = seqStorage.get(CLI_SEQ);
			if (requestSeq == null) {
				seqStorage.put(CLI_SEQ, 0L);
				requestSeq = 0L;
			}
		}
		return requestSeq;
	}

	public long increaseCliSeq(ConcurrentMap<String, Long> seqStorage) {
		requestSeq = getSequenceMessage(seqStorage) + 1;
		seqStorage.put(CLI_SEQ, requestSeq);
		return requestSeq;
	}
}
