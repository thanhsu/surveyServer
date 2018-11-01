package com.survey.internal.action;

import com.survey.constant.EventBusDiscoveryConst;
import com.survey.utils.VertxServiceCenter;

public class AccountBalanceProxyAction extends InternalSurveyBaseAction {

	@Override
	public void doProccess() {
		VertxServiceCenter.getInstance().sendNewMessage(EventBusDiscoveryConst.ETHEREUMPROXYDISCOVERY.name(),
				getMessageBody(), response);
	}

}
