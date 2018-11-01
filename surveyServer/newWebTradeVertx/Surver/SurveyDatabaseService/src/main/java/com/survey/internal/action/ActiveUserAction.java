package com.survey.internal.action;

import com.survey.dbservice.dao.UserDao;
import com.survey.utils.CodeMapping;
import com.survey.utils.FieldName;
import com.survey.utils.MessageDefault;

public class ActiveUserAction extends InternalSurveyBaseAction {

	@Override
	public void doProccess() {
		UserDao lvUserDao = new UserDao();
		lvUserDao.doGetUserInfobyUserName(getMessageBody().getString(FieldName.USERNAME));
		String token = getMessageBody().getString(FieldName.TOKEN);
		lvUserDao.getMvFutureResponse().setHandler(handler -> {
			if (handler.succeeded() && handler.result() != null) {
				if (!handler.result().getString(FieldName.CODE).equals(CodeMapping.U0000.toString())) {
					response.complete(
							MessageDefault.RequestFailed(CodeMapping.U000A.toString(), CodeMapping.U000A.value()));
				} else {
					if (token.equals(handler.result().getJsonObject(FieldName.DATA).getString(FieldName.TOKEN))) {
						UserDao lvUserDao1 = new UserDao();
						lvUserDao1.updateStatus(getMessageBody().getString(FieldName.USERNAME), "N");
						lvUserDao1.getMvFutureResponse().setHandler(handlerx -> {
							response.complete(handlerx.result());
						});
					} else {
						response.complete(
								MessageDefault.RequestFailed(CodeMapping.U000A.toString(), CodeMapping.U000A.value()));
					}
				}
			} else {
				response.complete(
						MessageDefault.RequestFailed(CodeMapping.U000A.toString(), CodeMapping.U000A.value()));
			}
		});
	}

}
