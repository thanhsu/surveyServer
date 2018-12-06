package com.survey.confirm.actions;

import com.survey.constant.UserNotificationEnum;
import com.survey.dbservice.dao.CashDepositDao;
import com.survey.notification.actions.NotifiAccountBalance;
import com.survey.notification.actions.NotifiCashDeposit;
import com.survey.notification.actions.NotifiNewAnswerData;
import com.survey.processor.bean.UserBalanceUpdateBean;
import com.survey.utils.ECashDepositType;
import com.survey.utils.FieldName;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.UpdateOptions;

public class ConfirmSurveyAnswer extends BaseConfirmAction {

	@Override
	public void doProcess(JsonObject msg) {
		String username = msg.getString(FieldName.USERNAME);
		String surveyID = msg.getString(FieldName.SURVEYID);
		String transID = msg.getString(FieldName.TRANID);
		double userBalance = Double.parseDouble(msg.getString(FieldName.USERBALANCE));
		double point = Double.parseDouble(msg.getString(FieldName.POINT));

		double surveyBalance = Double.parseDouble(msg.getString(FieldName.SURVEYBALANCE));
		boolean sucess = msg.getBoolean(FieldName.SUCCESS);

		if (sucess) {
			UserBalanceUpdateBean lvBalanceUpdateBean = new UserBalanceUpdateBean();
			lvBalanceUpdateBean.setAgent(surveyID);
			lvBalanceUpdateBean.setAgenttype("survey");
			lvBalanceUpdateBean.setAmount(point);
			lvBalanceUpdateBean.setBalance(userBalance);
			lvBalanceUpdateBean.setDw("D");
			lvBalanceUpdateBean.setMessage("Payment from answer survey");
			lvBalanceUpdateBean.setType(ECashDepositType.SURVEYANSWER.name());
			lvBalanceUpdateBean.setUsername(username);

			NotifiAccountBalance lvAccountBalance = new NotifiAccountBalance(lvBalanceUpdateBean);
			lvAccountBalance.generate();

		}

		CashDepositDao lvCashDepositDao = new CashDepositDao();
		lvCashDepositDao.updateDocument(new JsonObject().put(FieldName._ID, transID),
				new JsonObject().put(FieldName.SETTLESTATUS, sucess ? "S" : "U").put(FieldName.AMOUNT, point),
				new UpdateOptions(false), handler -> {
					if (handler.succeeded()) {
						NotifiCashDeposit lvNotifiCashDeposit = new NotifiCashDeposit(transID);
						lvNotifiCashDeposit.setType(UserNotificationEnum.ANSWERSURVEYPAYOUT);
						lvNotifiCashDeposit.generate();

						CashDepositDao lvCashDepositDao2 = new CashDepositDao();
						lvCashDepositDao2.queryDocument(new JsonObject().put(FieldName._ID, transID), h2 -> {
							if (h2.succeeded()) {
								String answerId = h2.result().get(0).getString(FieldName.ANSWERID);
								NotifiNewAnswerData lvNotifiNewAnswerData = new NotifiNewAnswerData();
								lvNotifiNewAnswerData.setAnswerID(answerId);
								lvNotifiNewAnswerData.setAmount(point);
								lvNotifiNewAnswerData.setTranID(transID);
								lvNotifiNewAnswerData.generate();
							}
						});
					}
				});
	}

}
