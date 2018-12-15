package com.survey.etheaction;

public class ProxyHoldMoney extends BaseEtheProxyAction {
	private String username;
	private String amount;
	private String transid;

	public ProxyHoldMoney() {
		action = "hold";
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getAmount() {
		return amount;
	}

	public void setAmount(String amount) {
		this.amount = amount;
	}

	public String getTransid() {
		return transid;
	}

	public void setTransid(String transid) {
		this.transid = transid;
	}

}
