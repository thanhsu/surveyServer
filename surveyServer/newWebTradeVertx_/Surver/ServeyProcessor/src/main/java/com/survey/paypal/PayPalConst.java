package com.survey.paypal;

import io.vertx.core.json.JsonObject;

public class PayPalConst {
	public static String total = "total";
	public static String currency = "currency";
	public static String details = "details";
	public static String allowed_payment_method = "allowed_payment_method";
	public static String items = "items";
	public static String links = "links";

	public static JsonObject urlPayment() {
		return new JsonObject().put("return_url", "paypal/res/return").put("cancel_url", "paypal/res/cancel");
	}
}
