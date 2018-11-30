package com.survey.etheaction;

public class ProxySurveyTransactionGet extends BaseEtheProxyAction {
	private String surveyid;
	private String fromtime;
	private String totime;
	private String type;

	public ProxySurveyTransactionGet(String pSurveyid, String pFromtime, String pTotime, String pType) {
		action = ("surveytransactionget");
		this.setSurveyid(pSurveyid);
		this.setFromtime(pFromtime);
		this.setTotime(pTotime);
		this.setType(pType);
	}

	public String getSurveyid() {
		return surveyid;
	}

	public void setSurveyid(String surveyid) {
		this.surveyid = surveyid;
	}

	public String getFromtime() {
		return fromtime;
	}

	public void setFromtime(String fromtime) {
		this.fromtime = fromtime;
	}

	public String getTotime() {
		return totime;
	}

	public void setTotime(String totime) {
		this.totime = totime;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

}
