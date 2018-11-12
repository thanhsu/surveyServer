package com.survey.internal.action;

import com.survey.dbservice.dao.SurveyDao;
import com.survey.utils.CodeMapping;
import com.survey.utils.FieldName;
public class DeleteSurveyAction extends InternalSurveyBaseAction {

	@Override
	public void doProccess() {
		String sername = getMessageBody().getString(FieldName.USERNAME);
		io.vertx.core.json.JsonArray listSurveyID = getMessageBody().getJsonArray(FieldName.LISTSURVEYID);
		String remark = getMessageBody().getString(FieldName.REMARK);
		
		for (int i=0; i< listSurveyID.size();i++) {
			SurveyDao  lvDao = new SurveyDao();
			lvDao.deleteSurvey(sername, listSurveyID.getString(i), remark);
		}
		this.CompleteGenerateResponse(CodeMapping.C0000.toString(), "Success", null, response);
		
	}

}
