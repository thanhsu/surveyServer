package com.survey.etheaction;

public class ProxyTransactionTransfer extends BaseEtheProxyAction {
	private String fromuser;
	private String touser;
	private String amount;
	private String transid;

	public ProxyTransactionTransfer(String pFromuser, String pTouser, String pAmout, String pTransid) {
		this.setFromuser(pFromuser);
		this.setTouser(pTouser);
		this.setAmount(pAmout);
		this.setTransid(pTransid);
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

}
