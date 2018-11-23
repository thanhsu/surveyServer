package com.survey.internal.action;

import com.survey.dbservice.dao.SurveyDao;
import com.survey.dbservice.dao.SurveySubmitDao;
import com.survey.utils.CodeMapping;
import com.survey.utils.FieldName;

import io.vertx.core.json.JsonObject;

public class RetrieveSurveyAnsweredAction extends InternalSurveyBaseAction {

	@Override
	public void doProccess() {
		String username = getMessageBody().getString(FieldName.USERNAME);
		String surveyID = getMessageBody().getString(FieldName.SURVEYID);

		SurveyDao lvSurveyDao = new SurveyDao();
		lvSurveyDao.retrieveSurvey(new JsonObject().put(FieldName._ID, surveyID).put(FieldName.USERNAME, username),
				h -> {
					if (h.succeeded()) {
						if (h.result().isEmpty()) {
							this.CompleteGenerateResponse(CodeMapping.C6666.toString(), CodeMapping.C6666.value(), null,
									response);
						} else {
							SurveySubmitDao lvDao = new SurveySubmitDao();
							lvDao.retrieveAllSubmitted(surveyID).setHandler(handler->{
								// JsonObject dt = new JsonObject();
								// dt.put(FieldName.SURVEYDATA, h.result().get(0));
								// dt.put(FieldName.RESPONSE, handler.result());
								this.CompleteGenerateResponse(CodeMapping.C0000.toString(),"OK", handler.result(), response);
							});
						}
					} else {
						this.CompleteGenerateResponse(CodeMapping.C6666.toString(), CodeMapping.C6666.value(), null,
								response);
					}
				});
	}

}
