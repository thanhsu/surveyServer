package com.survey.internal.action;

import com.survey.dbservice.dao.CashDepositDao;
import com.survey.dbservice.dao.SurveyDao;
import com.survey.etheaction.ProxyStopSurvey;
import com.survey.utils.CodeMapping;
import com.survey.utils.ECashDepositType;
import com.survey.utils.FieldName;

import io.vertx.core.json.JsonObject;

public class StopPushlishAction extends InternalSurveyBaseAction {

	@Override
	public void doProccess() {
		String username = getMessageBody().getString(FieldName.USERNAME);
		String surveyID = getMessageBody().getString(FieldName.SURVEYID);
		String remark = getMessageBody().getString(FieldName.REMARK);
		SurveyDao lvSurveyDao = new SurveyDao();
		lvSurveyDao.retrieveSurvey(new JsonObject().put(FieldName.USERNAME, username).put(FieldName._ID, surveyID),
				h -> {
					if (h.result() != null) {
						if (!h.result().isEmpty()) {
							String state = h.result().get(0).getString(FieldName.STATE);
							if (state.equals("A") || state.equals("C")) {
								CashDepositDao lvCashDepositDao = new CashDepositDao();
								lvCashDepositDao.createSurveyWithdraw(surveyID, username, ECashDepositType.SURVEYREFUND)
										.setHandler(handler -> {
											if (handler.result() != null) {
												String tranID = handler.result();
												ProxyStopSurvey lvProxyStopSurvey = new ProxyStopSurvey(surveyID,
														username, tranID);
												lvProxyStopSurvey.sendToProxyServer().setHandler(handler2 -> {
													if (handler2.result() != null) {
														if (handler2.result().getString(FieldName.CODE)
																.equals("P0000")) {
															// Update survey state = D
															SurveyDao lvSurveyDao2 = new SurveyDao();
															lvSurveyDao2.closesurvey(username, surveyID, true, remark);
															this.CompleteGenerateResponse(CodeMapping.C0000.name(),
																	"Success", new JsonObject().put(FieldName.TRANID,
																			handler.result()),
																	response);
															return;
														}
													}
													CashDepositDao lvCashDepositDao2 = new CashDepositDao();
													lvCashDepositDao2.delteDocument(
															new JsonObject().put(FieldName._ID, tranID), handler3 -> {
															});
													this.CompleteGenerateResponse(CodeMapping.P2222.name(),
															CodeMapping.P2222.value(),
															handler2.result().getJsonObject(FieldName.DATA), response);

												});
											} else {
												this.CompleteGenerateResponse(CodeMapping.C1111.name(),
														"Survey is being stop", h.result().get(0), response);
											}
										});
								// ProxyStopSurvey lvProxyStopSurvey = new ProxyStopSurvey(surveyID, username,
								// pTransID)
							} else {
								this.CompleteGenerateResponse(CodeMapping.S0002.name(), "Survey is being stop",
										h.result().get(0), response);
							}

							return;
						}
					}
					this.CompleteGenerateResponse(CodeMapping.S1111.name(), CodeMapping.S1111.value(), null, response);
				});

	}

}
