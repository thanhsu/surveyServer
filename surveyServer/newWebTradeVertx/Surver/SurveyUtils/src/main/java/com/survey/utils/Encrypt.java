package com.survey.utils;

import com.txtech.common.encryption.PasswordEncryptorInterface;

public class Encrypt {
	public static String encode(String input)
			throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		String output = "";
		PasswordEncryptorInterface lvExternalPasswordEncryptClass = (PasswordEncryptorInterface) Class
				.forName("com.txtech.common.encryption.SHA2PasswordEncryptor").newInstance();
		output = lvExternalPasswordEncryptClass.encryptPassword(input);
		return output;
	}

	public static Boolean compare(String input, String secret) {
		try {
			if (encode(input).equals(secret)) {
				return true;
			}
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		return false;

	}
}
