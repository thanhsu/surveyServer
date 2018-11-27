package com.survey.confirm.actions;

import com.survey.dbservice.dao.SurveyDao;
import com.survey.notification.actions.NotifiSurveyPushlished;
import com.survey.utils.FieldName;

import io.vertx.core.json.JsonObject;

public class PublishSurveyResult extends BaseConfirmAction {

	@Override
	public void doProcess(JsonObject msg) {
		String surveyID = msg.getString(FieldName.SURVEYID);
		boolean success = msg.getBoolean(FieldName.SUCCESS);
		JsonObject data = new JsonObject();
		if(success) {
			data.put(FieldName.STATUS, "N");
			
		}else {
			data.put(FieldName.STATUS, "L");
			data.put(FieldName.PUSHLISHREJECTCAUSE, msg.getString(FieldName.MESSAGE));
		}
		SurveyDao  lvDao = new SurveyDao();
		lvDao.updateSurveyData(surveyID,data );
		if(success){
			NotifiSurveyPushlished lvPushlished = new NotifiSurveyPushlished(surveyID);

			lvPushlished.generate();
		}else {
			NotifiSurveyPushlished lvPushlished = new NotifiSurveyPushlished(surveyID);
			lvPushlished.setPrivate(true);
			lvPushlished.setPublic(false);
			lvPushlished.generate();
			
		}
	}

}
