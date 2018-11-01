package com.survey.otp;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;

import com.survey.utils.Log;

public class AESEncryptor implements IEncryptor {

	private StandardPBEStringEncryptor encrytor;

	/**
	 * The logger for this class
	 */

	public AESEncryptor() {
		encrytor = new StandardPBEStringEncryptor();
		encrytor.setAlgorithm("PBEWithMD5AndDES");
		encrytor.setPassword("ZgPiPSCdq88K8Mfay7T7IX");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gslab.otp.IEncryptor#encrypt(java.lang.String)
	 */
	public String encrypt(String sharedSecret) throws OTPManagerException {
		Log.print("Encrypting shared Secret...");
		if (sharedSecret == null || sharedSecret.isEmpty()) {
			Log.print("SharedSecret cannot be null or empty");
			throw new OTPManagerException("SharedSecret cannot be null or empty");
		}
		String encryptedKey = null;
		try {
			encryptedKey = encrytor.encrypt(sharedSecret);
		} catch (Exception e) {
			Log.print("Error while encrypting sharedSecret " + e.getMessage());
			throw new OTPManagerException("Error while encrypting sharedSecret " + e.getMessage(), e);
		}
		Log.print("Encrypted Shared Secret successfully");
		return encryptedKey;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gslab.otp.IEncryptor#decrypt(java.lang.String)
	 */
	public String decrypt(String sharedSecret) throws OTPManagerException {
		Log.print("Decrypting shared Secret...");
		String decryptedKey = null;
		if (sharedSecret == null || sharedSecret.isEmpty()) {
			Log.print("SharedSecret cannot be null or empty");
			throw new OTPManagerException("SharedSecret cannot be null or empty");
		}
		try {
			decryptedKey = encrytor.decrypt(sharedSecret);
		} catch (Exception e) {
			// for Invalid sharedSecret, e.getMessage will be null
			Log.print("Error while decrypting sharedSecret " + e.getMessage());
			throw new OTPManagerException("Error while decrypting sharedSecret " + e.getMessage(), e);
		}
		Log.print("Decrypted Shared Secret successfully");
		return decryptedKey;
	}
}
