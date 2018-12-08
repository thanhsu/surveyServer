package com.survey.etheaction;

public class ProxyCashWithdrawWithSystem extends BaseEtheProxyAction {
	private String fromuser;
	private String touser;
	private String amount;
	private String transid;
	private String trantype;
	private String type = "W";

	public ProxyCashWithdrawWithSystem() {
		action = "cashtranser";
		type = "W";
		touser = "";
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

	public String getTouser() {
		return touser;
	}

	public void setTouser(String touser) {
		this.touser = touser;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getTrantype() {
		return trantype;
	}

	public void setTrantype(String trantype) {
		this.trantype = trantype;
	}

}
