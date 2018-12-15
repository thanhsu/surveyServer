package com.survey.etheaction;

import com.survey.utils.ECashTranType;

public class ProxyCashDepositWithSystem extends BaseEtheProxyAction {
	private String fromuser;
	private String touser;
	private String amount;
	private String transid;
	private String trantype;
	private String type = "D";
	
	public ProxyCashDepositWithSystem() {
		action = "cashtranser";
		type = "D";
		fromuser = "";
		trantype = ECashTranType.CASHDEPOSIT.name();
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

	public String getTrantype() {
		return trantype;
	}

	public void setTrantype(String trantype) {
		this.trantype = trantype;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

}
