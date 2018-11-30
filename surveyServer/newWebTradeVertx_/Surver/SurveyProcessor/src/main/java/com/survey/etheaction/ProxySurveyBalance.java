package com.survey.etheaction;

public class ProxySurveyBalance extends BaseEtheProxyAction {
	private String surveyid;

	public ProxySurveyBalance(String pSurveyID) {
		action = ("surveybalance");
		this.setSurveyid(pSurveyID);
	}

	public String getSurveyid() {
		return surveyid;
	}

	public void setSurveyid(String surveyid) {
		this.surveyid = surveyid;
	}

}
