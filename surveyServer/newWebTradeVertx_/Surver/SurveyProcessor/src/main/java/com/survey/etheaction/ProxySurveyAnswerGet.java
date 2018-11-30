package com.survey.etheaction;

public class ProxySurveyAnswerGet extends BaseEtheProxyAction {
	private String surveyid;
	
	public ProxySurveyAnswerGet(String pSurveyID) {
		action = ("surveyanswerget");
		this.setSurveyid(pSurveyID);
	}

	public String getSurveyid() {
		return surveyid;
	}

	public void setSurveyid(String surveyid) {
		this.surveyid = surveyid;
	}
	
	
}
