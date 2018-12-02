package com.survey.otp;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Date;

import org.apache.commons.codec.binary.Base32;
import org.apache.commons.codec.digest.DigestUtils;

import com.survey.utils.Log;
import com.txtech.common.encryption.SHA2PasswordEncryptor;

public class Utils {
	public static SecureRandom secureRandom = new SecureRandom();
	private static SHA2PasswordEncryptor mvHashSHA2 = new SHA2PasswordEncryptor();

	public static String generateSharedSecret() throws OTPManagerException {
		Log.print("Generating shared secret...");

		int value = Constants.SECRET_BITS / 8 + Constants.SCRATCH_CODES * Constants.BYTES_PER_SCRATCH_CODE;
		byte[] sharedSecret = null;
		byte[] encodedSharedSecret = null;

		Base32 codec = new Base32();
		try {
			secureRandom = SecureRandom.getInstance(Constants.RANDOM_NUMBER_ALGORITHM);
			byte[] buffer = new byte[value];
			secureRandom.nextBytes(buffer);
			sharedSecret = Arrays.copyOf(buffer, Constants.SECRET_BITS / 8);
			encodedSharedSecret = codec.encode(sharedSecret);
			// reSeed();
		} catch (Exception e) {
			Log.print("Error while generating shared secret " + e.getMessage());
			throw new OTPManagerException("Error while generating shared secret " + e.getMessage(), e);
		}
		Log.print("Generated shared secret successfully");
		return new String(encodedSharedSecret);
	}

	public static String[] generateOTPSMSMessage(int numDigits) {
		String[] lvSecretAndPublic = new String[2];
		StringBuilder generatedToken = new StringBuilder();
		String encodedSharedSecret = "";
		if (secureRandom == null) {
			try {
				secureRandom = SecureRandom.getInstance(Constants.RANDOM_NUMBER_ALGORITHM);
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		for (int i = 0; i < numDigits; i++) {
			generatedToken.append(secureRandom.nextInt(9));
		}
		try {
			encodedSharedSecret = OTPEncryptor.encryptPassword(generatedToken.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		lvSecretAndPublic[1] = generatedToken.toString();
		lvSecretAndPublic[0] = encodedSharedSecret;
		return lvSecretAndPublic;
	}

	public static boolean checkOTPSMSMessage(String otp, String ousSecret) {
		return OTPEncryptor.checkOTP(otp, ousSecret);
	}
	/*
	 * private static void reSeed() {
	 * secureRandom.setSeed(secureRandom.generateSeed(Constants.SEED_SIZE)); }
	 */

	public static void main(String[] args) {
		try {
			long t = new Date().getTime();
			System.out.println("start: " + t);
			String x = Utils.generateSharedSecret();
			String[] y = Utils.generateOTPSMSMessage(6);
			String sha256hex = DigestUtils.sha512Hex(x);

			System.out.println("spent: " + (new Date().getTime() - t));
			System.out.println("X: " + x);
			System.out.println("y0: " + y[0]);
			System.out.println("y1: " + y[1]);
			System.out.println("sha256 : " + sha256hex);

		} catch (OTPManagerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
