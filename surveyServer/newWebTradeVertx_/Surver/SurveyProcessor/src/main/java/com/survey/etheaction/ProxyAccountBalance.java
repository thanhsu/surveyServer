package com.survey.etheaction;

public class ProxyAccountBalance extends BaseEtheProxyAction {
	private String username;

	public ProxyAccountBalance(String username) {
		action = ("accountbalance");
		this.setUsername(username);
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

}
