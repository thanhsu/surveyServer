package com.survey.etheaction;

public class ActiveUserProxy extends BaseEtheProxyAction {
	private String username;
	private String email;

	public ActiveUserProxy(String pUsername, String pEmail) {
		action =("createaccount");
		this.setUsername(pUsername);
		this.setEmail(pEmail);
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

}
