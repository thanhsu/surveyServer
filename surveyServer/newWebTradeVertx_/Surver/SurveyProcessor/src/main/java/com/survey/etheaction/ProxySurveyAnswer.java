package com.survey.etheaction;

import io.vertx.core.json.JsonObject;

public class ProxySurveyAnswer extends BaseEtheProxyAction {
	private String surveyid;
	private String username;
	private String transid;
	private JsonObject data = new JsonObject();
	
	public ProxySurveyAnswer(String pSurveyID, String pUsername, String pTransID, JsonObject pData) {
		action = ("surveyanswer");
		this.setSurveyid(pSurveyID);
		this.setUsername(pUsername);
		this.setTransid(pTransID);
		this.setData(pData);
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
	public JsonObject getData() {
		return data;
	}
	public void setData(JsonObject data) {
		this.data = data;
	}
}
