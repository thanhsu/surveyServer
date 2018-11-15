package com.survey.constant;

import java.io.Serializable;

public class PushResponseJSONBean implements Serializable, JacksonEncoder.Encodable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -867891802275118351L;

	/**
	 *
	 */

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}


	public void setResult(Object result) {
		this.result = result;
	}

	private final String RESULT_SUCCESS = "success";
	String status;
	int code;
	Object result = new Object();

	public PushResponseJSONBean(String status, int code, Object obj) {
		super();
		this.status = status;
		this.code = code;
		String resultKey = "";
		if (obj == null) {
			result = null;
		} else {
			result = obj;
		}
	}

	public String getStatus() {
		return this.status;
	}

	public void setStatus(String stt) {
		this.status = stt;
	}

	public Object getResult() {
		return this.result;
	}
}
