package com.survey.internal.action;

import com.survey.dbservice.dao.UtilsDao;
import com.survey.utils.FieldName;
import com.survey.utils.MessageDefault;

public class RetrievePointValueAction extends InternalSurveyBaseAction {

	@Override
	public void doProccess() {
		boolean isHistory = getMessageBody().getBoolean(FieldName.ISHISTORY) == null ? false
				: getMessageBody().getBoolean(FieldName.ISHISTORY);
		UtilsDao lvDao = new UtilsDao();
		lvDao.retrieveEthePointValue(isHistory).setHandler(handler -> {
			if (handler.succeeded()) {
				response.complete(handler.result());
			} else {
				response.complete(MessageDefault.RequestFailed(handler.cause().getMessage()));
			}
		});
	}

}
