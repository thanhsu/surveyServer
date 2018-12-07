package com.survey.etheaction;

public class ProxyTransactionTransfer extends BaseEtheProxyAction {
	private String fromuser;
	private String touser;
	private String amount;
	private String transid;
	private String type;

	public ProxyTransactionTransfer(String pFromuser, String pTouser, String pAmout, String pTransid, String type) {
		action="cashtranser";
		this.setFromuser(pFromuser);
		this.setTouser(pTouser);
		this.setAmount(pAmout);
		this.setTransid(pTransid);
		this.setType(type);
	}

	public String getFromuser() {
		return fromuser;
	}

	public void setFromuser(String fromuser) {
		this.fromuser = fromuser;
	}

	public String getTouser() {
		return touser;
	}

	public void setTouser(String touser) {
		this.touser = touser;
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

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

}
