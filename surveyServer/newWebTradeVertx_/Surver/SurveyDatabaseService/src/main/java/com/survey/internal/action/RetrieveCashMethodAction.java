package com.survey.internal.action;

import com.survey.dbservice.dao.UtilsDao;
public class RetrieveCashMethodAction extends InternalSurveyBaseAction {

	@Override
	public void doProccess() {
		UtilsDao lvDao = new UtilsDao();
		lvDao.retrieveCashMethod().setHandler(handler -> {
			response.complete(handler.result());
		});
	}

}
