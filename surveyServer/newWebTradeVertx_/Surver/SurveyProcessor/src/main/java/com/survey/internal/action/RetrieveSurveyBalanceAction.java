package com.survey.internal.action;

import com.survey.dbservice.dao.SurveyDao;
import com.survey.etheaction.ProxySurveyBalance;
import com.survey.utils.CodeMapping;
import com.survey.utils.FieldName;

import io.vertx.core.json.JsonObject;

public class RetrieveSurveyBalanceAction extends InternalSurveyBaseAction {

	@Override
	public void doProccess() {
		String username = getMessageBody().getString(FieldName.USERNAME);
		String surveyID = getMessageBody().getString(FieldName.SURVEYID);
		SurveyDao lvSurveyDao = new SurveyDao();
		lvSurveyDao.retrieveSurvey(new JsonObject().put(FieldName.USERNAME, username).put(FieldName._ID, surveyID), h->{
			if(h.result()!=null) {
				if(!h.result().isEmpty()) {
					if(h.result().get(0).getString(FieldName.STATE).equals("A")||h.result().get(0).getString(FieldName.STATE).equals("C")) {
						ProxySurveyBalance lvProxySurveyBalance = new ProxySurveyBalance(surveyID);
						lvProxySurveyBalance.sendToProxyServer(response);
					}else {
						this.CompleteGenerateResponse(CodeMapping.S8888.name(), CodeMapping.S8888.value(), h.result().get(0), response);
					}
					return;
				}
			}
			this.CompleteGenerateResponse(CodeMapping.S1111.name(), CodeMapping.S1111.value(), null, response);
		});
		
	}

}
