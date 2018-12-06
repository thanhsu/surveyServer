package com.survey.constant;

public enum UserNotificationEnum {
	SURVEYSTATE("Survey state updated"),
	SURVEYPUSHLISH("Survey pushlish"),
	SURVEYSTOP("Survey stop"),
	SURVEYNEWRESPONSE("Survey new response"),
	ACCOUNT("Account Ssatus update"),
	ACCOUNTBALANCE("Account cash balance update"),
	CASHDEPOSIT("Account cash deposit"),
	CASHWITHDRAW ("Account cash withdraw"),
	ANSWERSURVEYPAYOUT("Payout for answer survey"),
	UNKNOWN("Unknownw");

	private String description;

	private UserNotificationEnum(String desp) {
		description = desp;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
