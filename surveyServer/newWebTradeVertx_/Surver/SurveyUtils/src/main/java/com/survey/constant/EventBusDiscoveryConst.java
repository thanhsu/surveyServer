package com.survey.constant;

public enum EventBusDiscoveryConst {
	SURVEYDBDISCOVERY("E.SURVEYDBEVENTBUS"), SURVEYINTERNALPROCESSORTDISCOVERY("E.INTERNALPROCESSOREVENTBUS")
	, MAILSERVICEDISCOVERY(""), 
	ETHEREUMPROXYDISCOVERY("E.EVENTBUSPROXYETHEREUMSERVER"),
	PAYPALGATEWAYDISCOVERY("E.PAYPALSERVICE"),
	SURVEYCONFIRMPROCESSORDISCOVERY("E.CONFIRMPROCESSOR"),
	SURVEYPUSHSERVERDISCOVEY("E.SURVEYPUSHSERVER")
	;

	private final String value;

	private EventBusDiscoveryConst(String v) {
		this.value = v;
	}

	public String value() {
		return this.value;
	}

	public static EventBusDiscoveryConst fromValue(String v) {
		try {
			return valueOf(v);
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public String toString() {
		return this.name();
	}
}
