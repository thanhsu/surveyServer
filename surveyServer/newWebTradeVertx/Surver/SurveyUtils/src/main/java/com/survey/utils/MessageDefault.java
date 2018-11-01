package com.survey.utils;

import io.vertx.core.json.JsonObject;

public class MessageDefault {
	public static JsonObject MethodNotSupport() {
		return new JsonObject().put(FieldName.CODE, CodeMapping.C5555.toString());
	}

	public static JsonObject ActionNotFound() {
		return new JsonObject().put(FieldName.CODE, CodeMapping.C2222.toString());
	}

	public static JsonObject ParamError() {
		return new JsonObject().put(FieldName.CODE, CodeMapping.C3333.toString()).put(FieldName.MESSAGE,
				CodeMapping.C3333.value());
	}

	public static JsonObject ParamError(String cause) {
		return new JsonObject().put(FieldName.CODE, CodeMapping.C4444.toString()).put(FieldName.MESSAGE, cause);
	}

	public static JsonObject RequestFailed(String cause) {
		return new JsonObject().put(FieldName.CODE, CodeMapping.C1111.toString()).put(FieldName.MESSAGE, cause);
	}

	public static JsonObject RequestFailed(String code, String cause) {
		return new JsonObject().put(FieldName.CODE, code).put(FieldName.MESSAGE, cause);
	}

	public static JsonObject PermissionError() {
		return new JsonObject().put(FieldName.CODE, CodeMapping.C6666.toString()).put(FieldName.MESSAGE,
				CodeMapping.C6666.value());
	}
	
	public static JsonObject SessionTimeOut() {
		return new JsonObject().put(FieldName.CODE, CodeMapping.C7777.toString()).put(FieldName.MESSAGE,
				CodeMapping.C7777.value());
	}
}
