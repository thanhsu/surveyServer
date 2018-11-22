package com.survey.constant;

public enum UserNotificationEnum {
	SURVEYSTATE("Survey state updated"),
	SURVEYPUSHLISH("Survey pushlish"),
	ACCOUNT("Account Ssatus update"),
	ACCOUNTCASH("Account cash balance update"),
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
