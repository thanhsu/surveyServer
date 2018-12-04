package com.survey.internal.action;

import com.survey.dbservice.dao.SurveyDao;
import com.survey.etheaction.ProxyAccountBalance;
import com.survey.etheaction.ProxySurveyBalance;
import com.survey.utils.CodeMapping;
import com.survey.utils.FieldName;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class AccountBalanceProxyAction extends InternalSurveyBaseAction {

	@Override
	public void doProccess() {
		ProxyAccountBalance lvProxyAccountBalance = new ProxyAccountBalance(
				getMessageBody().getString(FieldName.USERNAME));
		Future<JsonObject> lvAccountBalance = Future.future();
		lvProxyAccountBalance.sendToProxyServer(lvAccountBalance);

		Future<JsonObject> lvSurveyBalance = Future.future();
		SurveyDao lvSurveyDao = new SurveyDao();

		lvSurveyDao.retrieveSurveyPushlished(getMessageBody().getString(FieldName.USERNAME)).setHandler(h -> {
			JsonObject surveyBalance = new JsonObject();
			double totalSurveyBalance = 0;
			surveyBalance.put(FieldName.TOTALSURVEYBALANCE, totalSurveyBalance);
			surveyBalance.put(FieldName.LISTSURVEYBALANCE, new JsonArray());
			ProxySurveyBalance lvProxySurveyBalance = new ProxySurveyBalance("");
			if (h.result() != null) {
				for (int i = 0; i < h.result().size(); i++) {
					lvProxySurveyBalance.getListsurveyid().add(h.result().get(i).getString(FieldName._ID));
				}
				if (lvProxySurveyBalance.getListsurveyid().isEmpty()) {
					lvSurveyBalance.complete(surveyBalance);
				} else {
					lvProxySurveyBalance.sendToProxyServer().setHandler(h2 -> {
						if (h2.succeeded()) {
							if (h2.result().getString(FieldName.CODE).equals("P0000")) {
								double totalSurveyBalance2 = 0;
								JsonArray lst = h2.result().getJsonObject(FieldName.DATA)
										.getJsonArray(FieldName.RESULT);
								lst = (lst == null) ? new JsonArray() : lst;
								for (int i = 0; i < lst.size(); i++) {
									double lvTempBalance = Double
											.parseDouble(lst.getJsonObject(i).getValue(FieldName.BALANCE).toString());
									totalSurveyBalance2 += lvTempBalance;
								}
								surveyBalance.put(FieldName.TOTALSURVEYBALANCE, totalSurveyBalance2);
								surveyBalance.put(FieldName.LISTSURVEYBALANCE, lst);
							}
						}
						lvSurveyBalance.complete(surveyBalance);
					});
				}
			} else {
				lvSurveyBalance.complete(surveyBalance);
			}
		});

		CompositeFuture compositeFuture = CompositeFuture.all(lvAccountBalance, lvSurveyBalance);
		compositeFuture.setHandler(h -> {
			JsonObject result = new JsonObject();
			if (lvAccountBalance.succeeded()) {
				result.put(FieldName.ACCOUNTBALANCE, lvAccountBalance.result());
			} else {
				result.put(FieldName.ACCOUNTBALANCE, new JsonObject());
			}
			//
			if (lvSurveyBalance.succeeded()) {
				result.put(FieldName.SURVEYBALANCE, lvSurveyBalance.result());
			} else {
				result.put(FieldName.SURVEYBALANCE, new JsonObject());
			}
			this.CompleteGenerateResponse(CodeMapping.C0000.name(), "Account Balance Sumary", result, response);
		});

	}

}
