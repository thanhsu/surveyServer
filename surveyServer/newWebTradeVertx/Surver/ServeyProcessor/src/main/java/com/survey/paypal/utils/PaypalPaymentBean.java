package com.survey.paypal.utils;

import io.vertx.core.json.JsonObject;

public class PaypalPaymentBean {
	private String intent;
	private JsonObject payer;
	private TransactionBean transactions = new TransactionBean();
	private String note_to_payer;
	private JsonObject redirect_urls;
	public String getIntent() {
		return intent;
	}
	public void setIntent(String intent) {
		this.intent = intent;
	}
	public JsonObject getPayer() {
		return payer;
	}
	public void setPayer(JsonObject payer) {
		this.payer = payer;
	}
	public TransactionBean getTransactions() {
		return transactions;
	}
	public void setTransactions(TransactionBean transactions) {
		this.transactions = transactions;
	}
	public String getNote_to_payer() {
		return note_to_payer;
	}
	public void setNote_to_payer(String note_to_payer) {
		this.note_to_payer = note_to_payer;
	}
	public JsonObject getRedirect_urls() {
		return redirect_urls;
	}
	public void setRedirect_urls(JsonObject redirect_urls) {
		this.redirect_urls = redirect_urls;
	}
}
