package com.survey.internal.action;

import com.survey.dbservice.dao.FavouriteSurveyDao;
import com.survey.utils.CodeMapping;
import com.survey.utils.FieldName;

import io.vertx.core.json.JsonObject;

public class RemoveFavouriteSurveyAction extends InternalSurveyBaseAction {

	@Override
	public void doProccess() {
		String username = getMessageBody().getString(FieldName.USERNAME);
		io.vertx.core.json.JsonArray litSurvey = getMessageBody().getJsonArray(FieldName.LISTSURVEYID);
		FavouriteSurveyDao lvFavouriteSurveyDao = new FavouriteSurveyDao();
		lvFavouriteSurveyDao.removeFavouriteSurvey(username, litSurvey);
		this.CompleteGenerateResponse(CodeMapping.C0000.toString(), "", new JsonObject().put(FieldName.SUCCESS, true),
				response);
	}

}
