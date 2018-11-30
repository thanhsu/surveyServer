package com.survey.etheaction;

public class ProxySurveyDeposit extends BaseEtheProxyAction {
	private String surveyid;
	private String username;
	private String transid;
	private String point;

	public ProxySurveyDeposit(String pSurveyID, String pUsername, String pTransID, String pPoint) {
		action =("surveydeposit");
		this.setSurveyid(pSurveyID);
		this.setUsername(pUsername);
		this.setTransid(pTransID);
		this.setPoint(pPoint);
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

	public String getPoint() {
		return point;
	}

	public void setPoint(String point) {
		this.point = point;
	}

}
