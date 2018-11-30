package com.survey.confirm.actions;

import com.survey.dbservice.dao.CashTransactionDao;
import com.survey.notification.actions.NotifiAccountBalance;
import com.survey.processor.bean.UserBalanceUpdateBean;
import com.survey.utils.ECashWithdrawType;
import com.survey.utils.FieldName;

import io.vertx.core.json.JsonObject;

public class Transaction extends BaseConfirmAction {

	@Override
	public void doProcess(JsonObject msg) {
		String fromUser = msg.getString("fromUser");
		String toUser = msg.getString("toUser");
		String transID = msg.getString(FieldName.TRANID);
		double fromUserBalance = Double.parseDouble(msg.getString(FieldName.FROMUSERBALANCE));
		double toUserBalance = Double.parseDouble(msg.getString(FieldName.TOUSERBALANCE));
		boolean success = msg.getBoolean(FieldName.SUCCESS);
		double amount = Double.parseDouble(msg.getString(FieldName.VALUE));
		double fee = Double.parseDouble(msg.getString(FieldName.FEE));
		long timeStamp = Long.parseLong(msg.getString(FieldName.TIMESTAMP));
		CashTransactionDao lvCashTransaction = new CashTransactionDao();
		lvCashTransaction.updateCashTransferStatus(transID, success?"S":"U", timeStamp,amount,fee).setHandler(handler->{
			if(handler.succeeded()) {
				if(success) {
					UserBalanceUpdateBean lvFromUserBalance = new UserBalanceUpdateBean();
					lvFromUserBalance.setAgent(toUser);
					lvFromUserBalance.setDw("W");
					lvFromUserBalance.setAgenttype("account");
					lvFromUserBalance.setAmount(amount);
					lvFromUserBalance.setBalance(fromUserBalance);
					lvFromUserBalance.setType(ECashWithdrawType.TRANSFER.name());
					lvFromUserBalance.setUsername(fromUser);
					lvFromUserBalance.setMessage("Cash transfer to "+toUser);
					NotifiAccountBalance lvNotifiAccountBalance = new NotifiAccountBalance(lvFromUserBalance);
					lvNotifiAccountBalance.generate();
					
					UserBalanceUpdateBean lvtoUserBalance = new UserBalanceUpdateBean();
					lvtoUserBalance.setAgent(fromUser);
					lvtoUserBalance.setDw("D");
					lvtoUserBalance.setAgenttype("account");
					lvtoUserBalance.setAmount(amount);
					lvtoUserBalance.setBalance(toUserBalance);
					lvtoUserBalance.setType(ECashWithdrawType.TRANSFER.name());
					lvtoUserBalance.setUsername(toUser);
					lvtoUserBalance.setMessage("Cash transfer from "+fromUser);
					NotifiAccountBalance lvToNotifiAccountBalance = new NotifiAccountBalance(lvtoUserBalance);
					lvToNotifiAccountBalance.generate();
				}
			}
		});
		
	}

}
