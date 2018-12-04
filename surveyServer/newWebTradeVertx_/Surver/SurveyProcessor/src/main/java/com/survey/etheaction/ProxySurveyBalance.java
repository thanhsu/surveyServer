package com.survey.etheaction;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;


@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class ProxySurveyBalance extends BaseEtheProxyAction {
	private String surveyid;
	private List<String> listsurveyid = new ArrayList<>();

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

	public List<String> getListsurveyid() {
		return listsurveyid;
	}

	public void setListsurveyid(List<String> listsurveyid) {
		this.listsurveyid = listsurveyid;
	}

}
