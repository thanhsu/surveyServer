package com.survey.paypal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.paypal.api.payments.Amount;
import com.paypal.api.payments.Currency;
import com.paypal.api.payments.Details;
import com.paypal.api.payments.Links;
import com.paypal.api.payments.Payer;
import com.paypal.api.payments.Payment;
import com.paypal.api.payments.PaymentExecution;
import com.paypal.api.payments.Payout;
import com.paypal.api.payments.PayoutBatch;
import com.paypal.api.payments.PayoutItem;
import com.paypal.api.payments.PayoutSenderBatchHeader;
import com.paypal.api.payments.RedirectUrls;
import com.paypal.api.payments.Transaction;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;
import com.survey.paypal.utils.PaypalPaymentBean;
import com.survey.paypal.utils.TransactionDetailBean;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;

public class PaypalConnection {
	private static PaypalConnection instance = null;
	private Vertx vertx;
	private JsonObject config;
	private String clientID;
	private String secret;
	private String mode;
	APIContext context;
	private String host = "https://api.sandbox.paypal.com";
	public static String paymentLink = "/v1/payments/payment";

	public static synchronized PaypalConnection getInstance() {
		if (instance == null) {
			instance = new PaypalConnection();
		}
		return instance;
	}

	public void init(JsonObject config, Vertx pVertx) {
		this.config = config;
		clientID = config.getString("PaypalClientID");
		secret = config.getString("PaypalSecret");
		host = config.getString("PaypalHost");
		mode = config.getString("PaypalMode");
		context = new com.paypal.base.rest.APIContext(clientID, secret, config.getString("PaypalMode", "sandbox"));
		vertx = pVertx;
	}

	public Future<JsonObject> createPayment(String id, String fee, String total, String subTotal, String tax,
			String des, String remark, String ccy, JsonObject items) {
		Future<JsonObject> lvRS = Future.future();
		PaypalPaymentBean lvBean = new PaypalPaymentBean();
		lvBean.setIntent("sale");
		lvBean.setPayer(new JsonObject());
		TransactionDetailBean lvTransactionDetailBean = new TransactionDetailBean();
		lvTransactionDetailBean.setFee(fee);
		lvTransactionDetailBean.setSubtotal(subTotal);
		lvTransactionDetailBean.setTax(tax);
		lvBean.getTransactions().setAmount(new JsonObject().put(PayPalConst.total, total).put(PayPalConst.currency, ccy)
				.put(PayPalConst.details, JsonObject.mapFrom(lvTransactionDetailBean)));
		lvBean.getTransactions().setDescription(des);
		lvBean.getTransactions().setCustom("");
		lvBean.getTransactions().setInvoice_number(id);
		lvBean.getTransactions()
				.setPayment_options(new JsonObject().put(PayPalConst.allowed_payment_method, "IMMEDIATE_PAY"));
		lvBean.getTransactions().setSoft_descriptor("123456789");
		lvBean.getTransactions().setItem_list(new JsonObject().put(PayPalConst.items, new JsonArray().add(items)));
		lvBean.setNote_to_payer(remark);
		lvBean.setRedirect_urls(PayPalConst.urlPayment());

		HttpClient lvHttpClient = vertx.createHttpClient();
		lvHttpClient.postAbs(host + paymentLink).putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
				.putHeader(HttpHeaders.AUTHORIZATION, secret);
		WebClient lvWebClient = WebClient.create(vertx);
		lvWebClient.postAbs(paymentLink).putHeader(HttpHeaders.CONTENT_TYPE.toString(), "application/json")
				.putHeader("AUTHORIZATION", secret).sendJson(JsonObject.mapFrom(lvBean), handler -> {
					if (handler.succeeded()) {
						// Success
						JsonObject resp = handler.result().bodyAsJsonObject();
						JsonArray link = resp.getJsonArray(PayPalConst.links);
					} else {
						// Error
					}
					lvWebClient.close();
				});
		return lvRS;
	}

	public Future<JsonObject> createPaymentSDK(String id, String fee, String total, String subTotal, String tax,
			String des, String remark, String ccy, JsonObject items, String cancelUrl, String returlUrl) {
		Future<JsonObject> lvRS = Future.future();
		// Define Payment
		Payer payer = new Payer();
		payer.setPaymentMethod("paypal");

		// Set redirect URLs
		RedirectUrls redirectUrls = new RedirectUrls();
		redirectUrls.setCancelUrl(cancelUrl);
		redirectUrls.setReturnUrl(returlUrl);

		// Set payment details
		Details details = new Details();
		details.setShipping("0");
		details.setSubtotal(subTotal);
		details.setTax("1");

		// Payment amount
		Amount amount = new Amount();
		amount.setCurrency(ccy);
		// Total must be equal to sum of shipping, tax and subtotal.
		amount.setTotal(total);
		amount.setDetails(details);

		Transaction transaction = new Transaction();
		transaction.setAmount(amount);
		transaction.setDescription("This is the payment transaction description.");

		// Add transaction to a list
		List<Transaction> transactions = new ArrayList<Transaction>();
		transactions.add(transaction);

		// Add payment details
		Payment payment = new Payment();
		payment.setIntent("sale");
		payment.setPayer(payer);
		payment.setRedirectUrls(redirectUrls);
		payment.setTransactions(transactions);

		// Create payment
		try {
			Payment createdPayment = payment.create(context);
			Iterator<Links> links = createdPayment.getLinks().iterator();
			lvRS.complete(new JsonObject().put("link", createdPayment.getLinks()));
			/*
			 * while (links.hasNext()) { Links link = links.next(); if
			 * (link.getRel().equalsIgnoreCase("approval_url")) {
			 * 
			 * } }
			 */
		} catch (PayPalRESTException e) {
			System.err.println(e.getDetails());
			lvRS.fail(e.getCause());

		}

		return lvRS;
	}

	public Future<JsonObject> createPayout(String tranID, String ccy, String amount, String recipientType,
			String reciver, String note) {
		Future<JsonObject> lvRS = Future.future();
		Payout payout = new Payout();

		PayoutSenderBatchHeader senderBatchHeader = new PayoutSenderBatchHeader();

		senderBatchHeader.setSenderBatchId(tranID).setEmailSubject("Survey Cash Withdraw");

		Currency amount1 = new Currency();
		amount1.setValue(amount).setCurrency(ccy);

		PayoutItem senderItem1 = new PayoutItem();

		senderItem1.setRecipientType(recipientType).setNote(note).setReceiver(reciver).setSenderItemId(tranID)
				.setAmount(amount1);
		List<PayoutItem> items = new ArrayList<PayoutItem>();
		items.add(senderItem1);
		payout.setSenderBatchHeader(senderBatchHeader).setItems(items);
		PayoutBatch batch = null;

		try {

			// ### Api Context
			// Pass in a `ApiContext` object to authenticate
			// the call and to send a unique request id
			// (that ensures idempotency). The SDK generates
			// a request id if you do not pass one explicitly.
			APIContext apiContext = new APIContext(clientID, secret, mode);

			// ###Create Batch Payout
			batch = payout.create(apiContext, new HashMap<String, String>());

			System.out.println("Payout Batch With ID: " + batch.getBatchHeader().getPayoutBatchId());
			String request = Payout.getLastRequest();
			String response = Payout.getLastResponse();

			System.out.println("Request: " + request);
			System.out.println("Response: " + response);
			JsonObject requst = new JsonObject(request);
			JsonObject responseMessage = new JsonObject(response);

			lvRS.complete(new JsonObject().put("request", request).put("response", response).put("tranID", tranID));
		} catch (PayPalRESTException e) {
			lvRS.fail(e.getMessage());
		}

		return lvRS;
	}

	public Future<JsonObject> excutePayment(String paymentID, String payerID) {
		Future<JsonObject> lvRS = Future.future();
		Payment payment = new Payment();
		payment.setId(paymentID);

		PaymentExecution paymentExecution = new PaymentExecution();
		paymentExecution.setPayerId(payerID);
		try {
			Payment createdPayment = payment.execute(context, paymentExecution);
			System.out.println(createdPayment);
		} catch (PayPalRESTException e) {
			System.err.println(e.getDetails());
		}
		return lvRS;
	}

}
