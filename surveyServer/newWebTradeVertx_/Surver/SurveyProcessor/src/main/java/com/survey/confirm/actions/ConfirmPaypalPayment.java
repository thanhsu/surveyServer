package com.survey.confirm.actions;

import com.survey.dbservice.dao.CashWithdrawDao;
import com.survey.etheaction.ProxyCashWithdrawWithSystem;
import com.survey.notification.actions.NotifiCashWithdraw;
import com.survey.utils.FieldName;

import io.vertx.core.json.JsonObject;

public class ConfirmPaypalPayment extends BaseConfirmAction {

	@Override
	public void doProcess(JsonObject msg) {
		System.out.println("Confirm messsage: " + msg.toString());
		JsonObject data = msg.getJsonObject(FieldName.DATA);
		String tranID = msg.getString(FieldName.TRANID);

		if (data.getString("transaction_status").equals("PENDING")) {
			return;
		}

		CashWithdrawDao lvCashWithdrawDao = new CashWithdrawDao();
		lvCashWithdrawDao.findOneByID(tranID).setHandler(handler -> {
			if (handler.result() != null) {
				if (handler.result().getString(FieldName.SETTLESTATUS).equals("S")
						|| handler.result().getString(FieldName.SETTLESTATUS).equals("U")) {
					return;
				}

				if (data.getString("transaction_status").equals("FAILED")) {
					CashWithdrawDao lvCashWithdrawDao2 = new CashWithdrawDao();
					ProxyCashWithdrawWithSystem lvProxyCashWithdrawWithSystem = new ProxyCashWithdrawWithSystem();
					lvProxyCashWithdrawWithSystem.setTransid(tranID);
					lvProxyCashWithdrawWithSystem.setFromuser(handler.result().getString(FieldName.USERNAME));
					lvProxyCashWithdrawWithSystem.setTransferSuccess(false);
					lvProxyCashWithdrawWithSystem.sendToProxyServer();
					
					lvCashWithdrawDao2.updateSettlesStatus(tranID, "U", "", "", "Paypal rejected");
					lvCashWithdrawDao2.getMvFutureResponse().setHandler(h2 -> {
						NotifiCashWithdraw lvNotifiCashWithdraw = new NotifiCashWithdraw(tranID);
						lvNotifiCashWithdraw.generate();
					});
				} else {
					CashWithdrawDao lvCashWithdrawDao2 = new CashWithdrawDao();

					ProxyCashWithdrawWithSystem lvProxyCashWithdrawWithSystem = new ProxyCashWithdrawWithSystem();
					lvProxyCashWithdrawWithSystem.setTransferSuccess(true);
					lvProxyCashWithdrawWithSystem.setTransid(tranID);
					lvProxyCashWithdrawWithSystem.setFromuser(handler.result().getString(FieldName.USERNAME));
					lvProxyCashWithdrawWithSystem.sendToProxyServer();
					
					lvCashWithdrawDao2.updateSettlesStatus(tranID, "S", "", "", "");
					lvCashWithdrawDao2.getMvFutureResponse().setHandler(h2 -> {
						NotifiCashWithdraw lvNotifiCashWithdraw = new NotifiCashWithdraw(tranID);
						lvNotifiCashWithdraw.generate();
					});
				}
			}
		});

	}

}
