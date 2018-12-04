package com.survey.confirm.actions;

import com.survey.dbservice.dao.CashDepositDao;
import com.survey.notification.actions.NotifiAccountBalance;
import com.survey.notification.actions.NotifiCashDeposit;
import com.survey.processor.bean.UserBalanceUpdateBean;
import com.survey.utils.ECashDepositType;
import com.survey.utils.FieldName;

import io.vertx.core.json.JsonObject;

public class ConfirmSurveyWithdraw extends BaseConfirmAction {

	@Override
	public void doProcess(JsonObject msg) {
		String username = msg.getString(FieldName.USERNAME);
		String surveyID = msg.getString(FieldName.SURVEYID);
		String transID = msg.getString(FieldName.TRANID);
		double point = Double.parseDouble(msg.getString(FieldName.POINT));
		double userBalance = Double.parseDouble(msg.getString(FieldName.USERBALANCE));

		boolean success = msg.getBoolean(FieldName.SUCCESS);
		if (success) {
			UserBalanceUpdateBean lvUserBalance = new UserBalanceUpdateBean();
			lvUserBalance.setUsername(username);
			lvUserBalance.setDw("D");
			lvUserBalance.setBalance(userBalance);
			lvUserBalance.setAmount(point);
			lvUserBalance.setMessage("Account balance update! From survey withdraw");
			lvUserBalance.setType(ECashDepositType.SURVEYWITHDRAW.name());
			lvUserBalance.setAgent(surveyID);
			lvUserBalance.setAgenttype("survey");

			NotifiAccountBalance lvNitifiAccountBalance = new NotifiAccountBalance(lvUserBalance);
			lvNitifiAccountBalance.generate();
		}
		CashDepositDao lvCashDepositDao = new CashDepositDao();
		lvCashDepositDao.updateSurveyWithdrawSettleStatus(transID, success ? "S" : "U").setHandler(handler -> {
			if (handler.succeeded() && handler.result() != null) {
				NotifiCashDeposit lvNotifiCashDeposit = new NotifiCashDeposit(transID);
				lvNotifiCashDeposit.generate();
			}
		});
	}

}