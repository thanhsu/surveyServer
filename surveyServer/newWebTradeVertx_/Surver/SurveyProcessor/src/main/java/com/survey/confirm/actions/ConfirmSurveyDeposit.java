package com.survey.confirm.actions;

import com.survey.dbservice.dao.CashWithdrawDao;
import com.survey.dbservice.dao.SurveyDao;
import com.survey.notification.actions.NotifiAccountBalance;
import com.survey.notification.actions.NotifiCashWithdraw;
import com.survey.processor.bean.UserBalanceUpdateBean;
import com.survey.utils.ECashDepositType;
import com.survey.utils.ECashWithdrawType;
import com.survey.utils.FieldName;

import io.vertx.core.json.JsonObject;

public class ConfirmSurveyDeposit extends BaseConfirmAction {

	@Override
	public void doProcess(JsonObject msg) {
		String username = msg.getString(FieldName.USERNAME);
		String surveyID = msg.getString(FieldName.SURVEYID);
		String transID = msg.getString(FieldName.TRANID);
		boolean success = msg.getBoolean(FieldName.SUCCESS);
		String point = msg.getString(FieldName.POINT);
		String userBalance = msg.getString(FieldName.USERBALANCE);
		String surveyBalance = msg.getString(FieldName.USERBALANCE);
		String code = msg.getString(FieldName.CODE);
		if (success) {
			UserBalanceUpdateBean lvUserBalance = new UserBalanceUpdateBean();
			lvUserBalance.setUsername(username);
			lvUserBalance.setDw("W");
			lvUserBalance.setBalance(Double.parseDouble(userBalance));
			lvUserBalance.setAmount(Double.parseDouble(point));
			lvUserBalance.setMessage("Account balance update! From survey deposit");
			lvUserBalance.setType(ECashWithdrawType.SURVEYDEPOSIT.name());
			lvUserBalance.setAgent(surveyID);
			lvUserBalance.setAgenttype("survey");

			NotifiAccountBalance lvNitifiAccountBalance = new NotifiAccountBalance(lvUserBalance);
			lvNitifiAccountBalance.generate();
		}

		SurveyDao lvSurveyDao = new SurveyDao();
		lvSurveyDao.findOneByID(surveyID).setHandler(handler -> {

		});

		CashWithdrawDao lvCashWithdrawDao = new CashWithdrawDao();
		lvCashWithdrawDao.updateTransStatus(transID, success ? "S" : "U", code).setHandler(h -> {
			NotifiCashWithdraw lvNotificationCashWithdraw = new NotifiCashWithdraw(transID);
			lvNotificationCashWithdraw.setUsername(username);
			lvNotificationCashWithdraw.generate();
		});

	}

}
