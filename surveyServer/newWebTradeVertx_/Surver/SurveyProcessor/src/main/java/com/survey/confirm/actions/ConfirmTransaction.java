package com.survey.confirm.actions;

import java.util.Date;

import com.survey.dbservice.dao.CashTransactionDao;
import com.survey.dbservice.dao.CashWithdrawDao;
import com.survey.notification.actions.NotifiAccountBalance;
import com.survey.notification.actions.NotifiBuyCard;
import com.survey.processor.bean.UserBalanceUpdateBean;
import com.survey.utils.ECashTranType;
import com.survey.utils.ECashWithdrawType;
import com.survey.utils.FieldName;

import io.vertx.core.json.JsonObject;

public class ConfirmTransaction extends BaseConfirmAction {
	// public static String cashtransaction =
	// ECashTranType.CASHTRANSACTION.toString();
	@Override
	public void doProcess(JsonObject msg) {
		String fromUser = msg.getString("fromUser");
		String toUser = msg.getString("toUser");
		String transID = msg.getString(FieldName.TRANID);
		String trantype = msg.getString(FieldName.TRANTYPE);
		double fromUserBalance = Double.parseDouble(msg.getString(FieldName.FROMUSERBALANCE));
		double toUserBalance = Double.parseDouble(msg.getString(FieldName.TOUSERBALANCE));
		boolean success = msg.getBoolean(FieldName.SUCCESS);
		double amount = Double.parseDouble(msg.getString(FieldName.VALUE));
		double fee = Double.parseDouble(msg.getString(FieldName.FEE));
		long timeStamp = new Date().getTime();
		if(msg.getString(FieldName.TIMESTAMP)!=null) {
			timeStamp = Long.parseLong(msg.getString(FieldName.TIMESTAMP));
		}

		if (trantype.equals(ECashTranType.CASHTRANSACTION.name())) {
			CashTransactionDao lvCashTransaction = new CashTransactionDao();
			lvCashTransaction.updateCashTransferStatus(transID, success ? "S" : "U", timeStamp, amount, fee)
					.setHandler(handler -> {
						if (handler.succeeded()) {
							if (success) {
								UserBalanceUpdateBean lvFromUserBalance = new UserBalanceUpdateBean();
								lvFromUserBalance.setAgent(toUser);
								lvFromUserBalance.setDw("W");
								lvFromUserBalance.setAgenttype("account");
								lvFromUserBalance.setAmount(amount);
								lvFromUserBalance.setBalance(fromUserBalance);
								lvFromUserBalance.setType(ECashWithdrawType.TRANSFER.name());
								lvFromUserBalance.setUsername(fromUser);
								lvFromUserBalance.setMessage("Cash transfer to " + toUser);
								NotifiAccountBalance lvNotifiAccountBalance = new NotifiAccountBalance(
										lvFromUserBalance);
								lvNotifiAccountBalance.generate();

								UserBalanceUpdateBean lvtoUserBalance = new UserBalanceUpdateBean();
								lvtoUserBalance.setAgent(fromUser);
								lvtoUserBalance.setDw("D");
								lvtoUserBalance.setAgenttype("account");
								lvtoUserBalance.setAmount(amount);
								lvtoUserBalance.setBalance(toUserBalance);
								lvtoUserBalance.setType(ECashWithdrawType.TRANSFER.name());
								lvtoUserBalance.setUsername(toUser);
								lvtoUserBalance.setMessage("Cash transfer from " + fromUser);
								NotifiAccountBalance lvToNotifiAccountBalance = new NotifiAccountBalance(
										lvtoUserBalance);
								lvToNotifiAccountBalance.generate();
							}
						}
					});
		} else if (trantype.equals(ECashTranType.CASHDEPOSIT.name())) {

		} else if (trantype.equals(ECashTranType.CASHWITHDRAW.name())) {

		} else if (trantype.equals(ECashTranType.BUYCARD.name())) {

			CashWithdrawDao lvCashWithdrawDao = new CashWithdrawDao();
			lvCashWithdrawDao.queryDocument(new JsonObject().put(FieldName._ID, transID), handler -> {
				if (handler.succeeded()) {
					if (!handler.result().isEmpty()) {
						String cardID = handler.result().get(0).getString(FieldName.CARDID);
						NotifiBuyCard lvNotifiBuyCard = new NotifiBuyCard(cardID, success);
						lvNotifiBuyCard.generate();
						if (success) {
							UserBalanceUpdateBean lvFromUserBalance = new UserBalanceUpdateBean();
							lvFromUserBalance.setAgent("system");
							lvFromUserBalance.setDw("W");
							lvFromUserBalance.setAgenttype("system");
							lvFromUserBalance.setAmount(amount);
							lvFromUserBalance.setBalance(fromUserBalance);
							lvFromUserBalance.setType(ECashWithdrawType.BUYCARD.name());
							lvFromUserBalance.setUsername(fromUser);
							lvFromUserBalance.setMessage("Buy card " + toUser);
							NotifiAccountBalance lvNotifiAccountBalance = new NotifiAccountBalance(lvFromUserBalance);
							lvNotifiAccountBalance.generate();
						}
						
					}
				}
			});
		}

	}

}
