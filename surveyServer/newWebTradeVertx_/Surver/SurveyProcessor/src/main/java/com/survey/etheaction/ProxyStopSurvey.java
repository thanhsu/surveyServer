package com.survey.etheaction;

public class ProxyStopSurvey extends BaseEtheProxyAction {
	private String surveyid;
	private String username;
	private String transid;
	
	public ProxyStopSurvey(String pSurveyID, String pUsername, String pTransID) {
		action = ("surveystop");
		this.setSurveyid(pSurveyID);
		this.setUsername(pUsername);
		this.setTransid(pTransID);
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
	public String getTransid() {
		return transid;
	}
	public void setTransid(String transid) {
		this.transid = transid;
	}
	
	
}
