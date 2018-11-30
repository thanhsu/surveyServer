package com.survey.etheaction;

public class ProxyUserInfo extends BaseEtheProxyAction {
	private String username;

	public ProxyUserInfo(String pUsername) {
		action =("userinfo");
		this.setUsername(pUsername);
	}
	
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}
	
}
