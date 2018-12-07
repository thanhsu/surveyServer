package com.survey.internal.action;

import com.survey.etheaction.ProxyTransactionHistory;
import com.survey.utils.FieldName;

public class EtheTransactionHistory extends InternalSurveyBaseAction {

	@Override
	public void doProccess() {
		String username = getMessageBody().getString(FieldName.USERNAME);
		ProxyTransactionHistory lvProxyTransactionHistory = new ProxyTransactionHistory(username);
		lvProxyTransactionHistory.sendToProxyServer().setHandler(handler->{
			response.complete(handler.result());
		});
	}

}
