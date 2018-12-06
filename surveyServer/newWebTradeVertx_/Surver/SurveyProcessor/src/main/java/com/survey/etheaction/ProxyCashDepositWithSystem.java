package com.survey.etheaction;

public class ProxyCashDepositWithSystem extends BaseEtheProxyAction {
	private String fromuser;
	private String amount;
	private String transid;
	public ProxyCashDepositWithSystem() {
		action = "depositsystem";
	}
	public String getFromuser() {
		return fromuser;
	}

	public void setFromuser(String fromuser) {
		this.fromuser = fromuser;
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