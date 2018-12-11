package com.survey.etheaction;

import com.survey.etheaction.BaseEtheProxyAction;

public class ProxyCreateSurvey extends BaseEtheProxyAction {
	private String surveyid;
	private String username;
	private double initialfund;
	private double limitfund;
	private boolean notify;
	private double payout;
	private double limitresponse;
	private String pushlishid;

	public ProxyCreateSurvey(String pSurveyID, String pUsername, double pInitialfund, double pLimitfund,
			boolean pNotify, double pPayout, double pLimitResponse, String pPushlishID) {
		this.setSurveyid(pSurveyID);
		this.setUsername(pUsername);
		this.setInitialfund(pInitialfund);
		this.setLimitfund(pLimitfund);
		this.setNotify(pNotify);
		this.setPayout(pPayout);
		this.setLimitresponse(pLimitResponse);
		this.setPushlishid(pPushlishID);
		action = "publishsurvey";
	}

	public String getSurveyid() {
		return surveyid;
	}

	public void setSurveyid(String surveyid) {
		this.surveyid = surveyid;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public double getInitialfund() {
		return initialfund;
	}

	public void setInitialfund(double initialfund) {
		this.initialfund = initialfund;
	}

	public double getLimitfund() {
		return limitfund;
	}

	public void setLimitfund(double limitfund) {
		this.limitfund = limitfund;
	}

	public boolean isNotify() {
		return notify;
	}

	public void setNotify(boolean notify) {
		this.notify = notify;
	}

	public double getPayout() {
		return payout;
	}

	public void setPayout(double payout) {
		this.payout = payout;
	}

	public double getLimitresponse() {
		return limitresponse;
	}

	public void setLimitresponse(double limitresponse) {
		this.limitresponse = limitresponse;
	}

	public String getPushlishid() {
		return pushlishid;
	}

	public void setPushlishid(String pushlishID) {
		this.pushlishid = pushlishID;
	}

}
