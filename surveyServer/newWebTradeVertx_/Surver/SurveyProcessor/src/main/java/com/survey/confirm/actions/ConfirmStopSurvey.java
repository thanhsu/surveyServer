package com.survey.confirm.actions;

import com.survey.constant.UserNotificationEnum;
import com.survey.dbservice.dao.CashDepositDao;
import com.survey.dbservice.dao.SurveyDao;
import com.survey.dbservice.dao.SurveyPushlishDao;
import com.survey.notification.actions.NotifiAccountBalance;
import com.survey.notification.actions.NotifiCashDeposit;
import com.survey.notification.actions.NotifiSurveyPushlished;
import com.survey.processor.bean.UserBalanceUpdateBean;
import com.survey.utils.ECashDepositType;
import com.survey.utils.FieldName;
import com.survey.utils.Log;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.UpdateOptions;

public class ConfirmStopSurvey extends BaseConfirmAction {

	@Override
	public void doProcess(JsonObject msg) {
		String surveyID = msg.getString(FieldName.SURVEYID);
		String transID = msg.getString(FieldName.TRANID);
		String username = msg.getString(FieldName.USERNAME);
		double point = 0;
		try {
			point = Double.parseDouble(msg.getValue(FieldName.REFUNDPOINT).toString());
		} catch (Exception e) {
			// TODO: handle exception
		}
		double userBalance = Double.parseDouble(msg.getString(FieldName.USERBALANCE));

		boolean success = msg.getBoolean(FieldName.SUCCESS);

		SurveyDao lvDao = new SurveyDao();
		JsonObject data = new JsonObject();
		if (success) {
			data.put(FieldName.STATUS, "S");
			lvDao.updateSurveyData(surveyID, data);
			SurveyPushlishDao lvSurveyPushlishDao = new SurveyPushlishDao();
			lvSurveyPushlishDao.updateDocument(new JsonObject().put(FieldName.SURVEYID, surveyID),
					new JsonObject().put(FieldName.STATE, "D"), new UpdateOptions(false), handler -> {
					});
		}

		NotifiSurveyPushlished lvPushlished = new NotifiSurveyPushlished(surveyID);
		lvPushlished.setLvNotificationEnum(UserNotificationEnum.SURVEYSTOP);
		lvPushlished.setSurveybalance(msg.getValue(FieldName.SURVEYBALANCE).toString());
		lvPushlished.setPrivate(true);
		lvPushlished.setPublic(false);
		lvPushlished.generate();

		Log.println("Received invalid pushlishID. Message: " + Json.encode(msg), Log.ACCESS_LOG);

		if (success) {
			UserBalanceUpdateBean lvUserBalance = new UserBalanceUpdateBean();
			lvUserBalance.setUsername(username);
			lvUserBalance.setDw("D");
			lvUserBalance.setBalance(userBalance);
			lvUserBalance.setAmount(point);
			lvUserBalance.setMessage("Account balance update! From survey withdraw");
			lvUserBalance.setType(ECashDepositType.SURVEYREFUND.name());
			lvUserBalance.setAgent(surveyID);
			lvUserBalance.setAgenttype("survey");

			NotifiAccountBalance lvNitifiAccountBalance = new NotifiAccountBalance(lvUserBalance);
			lvNitifiAccountBalance.generate();
		}
		CashDepositDao lvCashDepositDao = new CashDepositDao();
		lvCashDepositDao.updateSurveyWithdrawSettleStatus(transID, success ? "S" : "U", point).setHandler(handler -> {
			if (handler.succeeded() && handler.result() != null) {
				NotifiCashDeposit lvNotifiCashDeposit = new NotifiCashDeposit(transID);
				lvNotifiCashDeposit.generate();
			}
		});
	}

}
