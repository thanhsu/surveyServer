package com.survey.internal.action;

import com.survey.dbservice.dao.FavouriteSurveyDao;
import com.survey.utils.CodeMapping;
import com.survey.utils.FieldName;

import io.vertx.core.json.JsonObject;

public class AddFavouriteSurveyAction extends InternalSurveyBaseAction {

	@Override
	public void doProccess() {
		String username = getMessageBody().getString(FieldName.USERNAME);
		String surveyID = getMessageBody().getString(FieldName.SURVEYID);
		FavouriteSurveyDao lvFavouriteSurveyDao = new FavouriteSurveyDao();
		lvFavouriteSurveyDao.storeNewFavouriteSurvey(username, surveyID).setHandler(handler -> {
			if (handler.result()) {
				this.CompleteGenerateResponse(CodeMapping.C0000.toString(), "",
						new JsonObject().put(FieldName.SUCCESS, handler.result()), response);
			} else {
				this.CompleteGenerateResponse(CodeMapping.C1111.toString(), "",
						new JsonObject().put(FieldName.SUCCESS, handler.result()), response);
			}
		});

	}

}
