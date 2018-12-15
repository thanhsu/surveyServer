package com.survey.confirm.actions;
import com.survey.dbservice.dao.CashWithdrawDao;
import com.survey.notification.actions.NotifiCashWithdraw;
import com.survey.utils.FieldName;
import com.survey.utils.VertxServiceCenter;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

public class ConfirmTransactionHold extends BaseConfirmAction {

	@Override
	public void doProcess(JsonObject msg) {
		String transID = msg.getString(FieldName.TRANID);
		String username = msg.getString(FieldName.USERNAME);
		boolean success = msg.getBoolean(FieldName.SUCCESS);
		double value = Double.parseDouble(msg.getString(FieldName.VALUE));
		if (!success) {
			CashWithdrawDao lvCashWithdrawDao = new CashWithdrawDao();
			lvCashWithdrawDao.updateSettlesStatus(transID, "U", "", "", "");
			lvCashWithdrawDao.getMvFutureResponse().setHandler(handler -> {
				NotifiCashWithdraw lvNotifiCashWithdraw = new NotifiCashWithdraw(transID);
				lvNotifiCashWithdraw.generate();
			});
		} else {
			CashWithdrawDao lvCashWithdrawDao = new CashWithdrawDao();
			lvCashWithdrawDao.findOneByID(transID).setHandler(handler -> {
				if (handler.result() != null) {
					if (!handler.result().isEmpty()) {
						String discoveryKey = handler.result().getString(FieldName.DISCOVERYKEY);
						JsonObject message = handler.result().getJsonObject(FieldName.PAYMENTDETAIL);

						Future<JsonObject> lvFuture = Future.future();
						VertxServiceCenter.getInstance().sendNewMessage(discoveryKey, message, lvFuture);
						lvFuture.setHandler(sendToPaypal -> {
							CashWithdrawDao lvCashWithdrawDao2 = new CashWithdrawDao();
							if (sendToPaypal.succeeded()) {
								if (sendToPaypal.result().getBoolean("success")
										&& sendToPaypal.result().getJsonObject(FieldName.DATA) != null) {
									// Send to paypal success
									JsonObject data = sendToPaypal.result().getJsonObject(FieldName.DATA);
									JsonObject responseDt = data.getJsonObject("response");

									if (responseDt.getJsonObject("batch_header").getString("batch_status")
											.equals("PENDING")) {
										String link = responseDt.getJsonArray("links").getJsonObject(0)
												.getString("href");
										lvCashWithdrawDao2.updateSettlesStatusPend(message.getString(FieldName._ID),
												"P", message.getString(FieldName.IPADDRESS),
												message.getString(FieldName.MACADDRESS), link);
									} else {
										lvCashWithdrawDao2.updateSettlesStatus(message.getString(FieldName._ID), "U",
												message.getString(FieldName.IPADDRESS),
												message.getString(FieldName.MACADDRESS),
												responseDt.getJsonObject("batch_header").toString());
										lvCashWithdrawDao2.getMvFutureResponse().setHandler(h -> {
											NotifiCashWithdraw lvNotifiCashWithdraw = new NotifiCashWithdraw(transID);
											lvNotifiCashWithdraw.generate();
										});
									}

								} else {
									// Send to paypal fail
									// store cash transaction with state = U
									lvCashWithdrawDao2.updateSettlesStatus(message.getString(FieldName._ID), "U",
											message.getString(FieldName.IPADDRESS),
											message.getString(FieldName.MACADDRESS),
											sendToPaypal.result().getString("cause"));
									lvCashWithdrawDao2.getMvFutureResponse().setHandler(h -> {
										NotifiCashWithdraw lvNotifiCashWithdraw = new NotifiCashWithdraw(transID);
										lvNotifiCashWithdraw.generate();
									});
								}

								//
							} else {
								// Send to paypal failed
								// store cash transaction with state = U
								lvCashWithdrawDao2.updateSettlesStatus(message.getString(FieldName._ID), "U",
										message.getString(FieldName.IPADDRESS), message.getString(FieldName.MACADDRESS),
										sendToPaypal.cause().getMessage());
								lvCashWithdrawDao2.getMvFutureResponse().setHandler(h -> {
									NotifiCashWithdraw lvNotifiCashWithdraw = new NotifiCashWithdraw(transID);
									lvNotifiCashWithdraw.generate();
								});
							}
						});
					}
				}

			});

		}
	}

}
