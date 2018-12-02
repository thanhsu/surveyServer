package com.survey.internal.action;

import com.survey.dbservice.dao.SurveyDao;
import com.survey.utils.CodeMapping;
import com.survey.utils.FieldName;

import io.vertx.core.json.JsonObject;

public class RetrieveSurveyBaseInfoAction extends InternalSurveyBaseAction {

	@Override
	public void doProccess() {
		String surveyId = getMessageBody().getString(FieldName.SURVEYID);
		SurveyDao  lvSurveyDao = new SurveyDao();
		lvSurveyDao.retrieveSurvey(new JsonObject().put(FieldName._ID, surveyId), h->{
			if(h.result()!=null) {
				if(h.result().isEmpty()) {
					this.CompleteGenerateResponse(CodeMapping.S1111.name(), CodeMapping.S1111.value(), null, response);
				}else {
					this.CompleteGenerateResponse(CodeMapping.S0000.name(), "", h.result().get(0), response);
				}
			}else {
				this.CompleteGenerateResponse(CodeMapping.S1111.name(), CodeMapping.S1111.value(), null, response);
			}
		});
	}

}
