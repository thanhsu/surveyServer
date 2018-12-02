package com.survey.internal.action;

import com.survey.etheaction.ProxyAccountBalance;
import com.survey.utils.FieldName;

public class AccountBalanceProxyAction extends InternalSurveyBaseAction {

	@Override
	public void doProccess() {
		ProxyAccountBalance lvProxyAccountBalance = new ProxyAccountBalance(getMessageBody().getString(FieldName.USERNAME));
		lvProxyAccountBalance.sendToProxyServer(response);
	}

}
