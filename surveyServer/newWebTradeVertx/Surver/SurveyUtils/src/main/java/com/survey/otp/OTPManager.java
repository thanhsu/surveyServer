package com.survey.otp;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Date;
import java.util.concurrent.ConcurrentMap;

import com.survey.utils.ChronicleStorage;
import com.survey.utils.Log;

import io.vertx.core.json.JsonObject;

public class OTPManager {
	private static IEncryptor encryptor;
	private static JsonObject mvOTpConfig = new JsonObject();
	private static int mvOTPDigits = 6;
	private static OTPManager mvOTPService = null;
	private static String mvDefaultOTPChannel = "SMS";
	private static ConcurrentMap<String, OTPUser> mvOTPStorage = null;
	private static long mvMaxOTPTime = 300000;
	private static int maxFail = 5;

	public static OTPManager getInstance() {
		if (mvOTPService == null) {
			synchronized (OTPManager.class) {
				mvOTPService = new OTPManager();
				try {
					mvOTPService.init(encryptor, getMvOTpConfig());
				} catch (OTPManagerException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return mvOTPService;
	}

	public void init(IEncryptor encryptor, JsonObject pConfig) throws OTPManagerException {
		Log.print("Initializing properties file and references...Config: " + pConfig.encodePrettily());
		setMvOTpConfig(pConfig);
		setMvOTPDigits(getMvOTpConfig().getInteger("OTPDigits"));
		setMvMaxOTPTime(getMvOTpConfig().getInteger("OTPTimeOut"));
		setMaxFail(getMvOTpConfig().getInteger("MaxFail"));
		setMvDefaultOTPChannel(getMvOTpConfig().getString("defaultChannel"));
		try {
			Utils.secureRandom = SecureRandom.getInstance(Constants.RANDOM_NUMBER_ALGORITHM);
		} catch (NoSuchAlgorithmException e) {
			Log.print("Init Secure Random Failed.Cause: " + e.getMessage(), Log.ERROR_LOG);
			e.printStackTrace();
		}
		Log.print("Init SecureRandom Success");
		if (encryptor == null) {
			Log.print("No Reference found for IEncryptor, Setting  AESEncryptor by Default. ");
			encryptor = new AESEncryptor();
		}
		Log.print("Init IEncryptor Success");
		if (mvOTPStorage == null) {
			mvOTPStorage = ChronicleStorage.createOTPStorage(getMvOTpConfig().getString("CachingStoredFilePath"),
					getMvOTpConfig().getString("OTPStoredKey"));
		}
	}

	public String register(OTPUser userInfo) {
		Log.print("Registering user...");
		if (userInfo == null) {
			Log.print("UserInfo cannot be null");

		}
		String[] lvOTP = Utils.generateOTPSMSMessage(getMvOTPDigits());
		userInfo.setCreateDate(new Date());
		userInfo.setOtp(lvOTP[0]);
		try {
			userInfo.setSecret(Utils.generateSharedSecret());
		} catch (Exception e) {

		}
		return lvOTP[1];
	}

	public OTPVerifyResp verify(String secretKey, String otp) {
		OTPVerifyResp lvOTPVerifyResponse = new OTPVerifyResp();
		if (!getMvOTpConfig().getBoolean("enable")) {
			lvOTPVerifyResponse.setSuccess(true);
			lvOTPVerifyResponse.setFailRemain(0);
			lvOTPVerifyResponse.setMessage("0000");
			try {
				mvOTPStorage.remove(secretKey);
			} catch (Exception e) {
				mvOTPStorage = ChronicleStorage.createOTPStorage(getMvOTpConfig().getString("CachingStoredFilePath"),
						getMvOTpConfig().getString("OTPStoredKey"));
			}
			return lvOTPVerifyResponse;
		}
		if (mvOTPStorage == null) {
			mvOTPStorage = ChronicleStorage.createOTPStorage(getMvOTpConfig().getString("CachingStoredFilePath"),
					getMvOTpConfig().getString("OTPStoredKey"));
		}
		OTPUser lvOTPUser = mvOTPStorage.get(secretKey);
		if (lvOTPUser == null) {
			lvOTPVerifyResponse.setSuccess(false);
			lvOTPVerifyResponse.setFailRemain(-1);
			lvOTPVerifyResponse.setMessage("1111");
			return lvOTPVerifyResponse;
		}
		// check failed
		if (lvOTPUser.getFailedCount() >= getMaxFail()) {
			lvOTPVerifyResponse.setSuccess(false);
			lvOTPVerifyResponse.setFailRemain(0);
			lvOTPVerifyResponse.setMessage("1110");
			mvOTPStorage.remove(secretKey);
			return lvOTPVerifyResponse;
		}
		// check timeout
		if ((new Date().getTime() - lvOTPUser.getCreateDate().getTime()) > mvMaxOTPTime) {
			lvOTPVerifyResponse.setSuccess(false);
			lvOTPVerifyResponse.setFailRemain(-1);
			lvOTPVerifyResponse.setMessage("1010");
			mvOTPStorage.remove(secretKey);
			return lvOTPVerifyResponse;
		}
		if (Utils.checkOTPSMSMessage(otp, lvOTPUser.getOtp())) {
			lvOTPVerifyResponse.setSuccess(true);
			lvOTPVerifyResponse.setFailRemain(0);
			lvOTPVerifyResponse.setMessage("0000");
			mvOTPStorage.remove(secretKey);
			return lvOTPVerifyResponse;

		} else {
			lvOTPVerifyResponse.setSuccess(false);
			lvOTPUser.setFailedCount(lvOTPUser.getFailedCount() + 1);
			lvOTPVerifyResponse.setFailRemain(getMaxFail() - lvOTPUser.getFailedCount());
			lvOTPVerifyResponse.setMessage("1100");
			mvOTPStorage.replace(secretKey, lvOTPUser);
			return lvOTPVerifyResponse;
		}
	}

	public void saveOTPValue(OTPUser pOTpUser) {
		if (mvOTPStorage == null) {
			mvOTPStorage = ChronicleStorage.createOTPStorage(getMvOTpConfig().getString("CachingStoredFilePath"),
					getMvOTpConfig().getString("OTPStoredKey"));
		}
		mvOTPStorage.put(pOTpUser.getSecret(), pOTpUser);
	}

	public static JsonObject getMvOTpConfig() {
		return mvOTpConfig;
	}

	public static void setMvOTpConfig(JsonObject mvOTpConfig) {
		OTPManager.mvOTpConfig = mvOTpConfig;
	}

	public static int getMvOTPDigits() {
		return mvOTPDigits;
	}

	public static void setMvOTPDigits(int mvOTPDigits) {
		OTPManager.mvOTPDigits = mvOTPDigits;
	}

	public static long getMvMaxOTPTime() {
		return mvMaxOTPTime;
	}

	public static void setMvMaxOTPTime(long mvMaxOTPTime) {
		OTPManager.mvMaxOTPTime = mvMaxOTPTime;
	}

	public static int getMaxFail() {
		return maxFail;
	}

	public static void setMaxFail(int maxFail) {
		OTPManager.maxFail = maxFail;
	}

	public static String getMvDefaultOTPChannel() {
		return mvDefaultOTPChannel;
	}

	public static void setMvDefaultOTPChannel(String mvDefaultOTPChannel) {
		OTPManager.mvDefaultOTPChannel = mvDefaultOTPChannel;
	}

}
