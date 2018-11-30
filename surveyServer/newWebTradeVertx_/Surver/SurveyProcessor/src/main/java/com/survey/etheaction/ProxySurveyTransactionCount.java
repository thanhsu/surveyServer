package com.survey.etheaction;

public class ProxySurveyTransactionCount extends BaseEtheProxyAction {
	private String surveyid;
	
	public ProxySurveyTransactionCount(String pSurveyID) {
		this.setSurveyid(pSurveyID);
		action = ("surveytransactioncount");
	}

	public String getSurveyid() {
		return surveyid;
	}

	public void setSurveyid(String surveyid) {
		this.surveyid = surveyid;
	}

}
