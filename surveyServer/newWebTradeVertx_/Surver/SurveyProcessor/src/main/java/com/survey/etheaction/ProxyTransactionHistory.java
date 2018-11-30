package com.survey.etheaction;

public class ProxyTransactionHistory extends BaseEtheProxyAction {
	private String username;

	public ProxyTransactionHistory(String pUsername) {
		this.setUsername(pUsername);
		action = "transactionhistory";
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

}
