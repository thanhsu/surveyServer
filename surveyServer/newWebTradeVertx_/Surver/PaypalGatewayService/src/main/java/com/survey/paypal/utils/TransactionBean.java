package com.survey.paypal.utils;

import io.vertx.core.json.JsonObject;

public class TransactionBean {
	private JsonObject amount;
	private String description;
	private String custom;
	private String invoice_number;
	private JsonObject payment_options;
	private String soft_descriptor;
	private JsonObject item_list;

	public JsonObject getAmount() {
		return amount;
	}

	public void setAmount(JsonObject amount) {
		this.amount = amount;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getCustom() {
		return custom;
	}

	public void setCustom(String custom) {
		this.custom = custom;
	}

	public String getInvoice_number() {
		return invoice_number;
	}

	public void setInvoice_number(String invoice_number) {
		this.invoice_number = invoice_number;
	}

	public JsonObject getPayment_options() {
		return payment_options;
	}

	public void setPayment_options(JsonObject payment_options) {
		this.payment_options = payment_options;
	}

	public String getSoft_descriptor() {
		return soft_descriptor;
	}

	public void setSoft_descriptor(String soft_descriptor) {
		this.soft_descriptor = soft_descriptor;
	}

	public JsonObject getItem_list() {
		return item_list;
	}

	public void setItem_list(JsonObject item_list) {
		this.item_list = item_list;
	}

}
