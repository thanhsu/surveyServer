package com.survey.utils;

public class CashManualUtils {
	public static String genDepositRegex(String username, String tranID) {
		String lvTmp="";
		lvTmp += "Nop tien vao tai khoan "+username + " TRANSID "+ tranID;
		return lvTmp;
	}
}
