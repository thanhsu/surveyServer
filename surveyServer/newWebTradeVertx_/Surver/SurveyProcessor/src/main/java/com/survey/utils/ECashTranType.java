package com.survey.utils;

public enum ECashTranType {
	CASHTRANSACTION("cashtransaction"),
	BUYCARD("buycard"),
	CASHDEPOSIT("cashdeposit"),
	CASHWITHDRAW("cashwithdraw");
	
	String value;
	private ECashTranType(String pValue) {
		this.setValue(pValue);
	}
	public String getValue() {
		return "";
	}
	public void setValue(String value) {
		this.value = value;
	}
}
