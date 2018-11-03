package com.survey.dbservice.dao;

import com.survey.utils.FieldName;

import io.vertx.core.json.JsonObject;

public class ProxyLogDao extends SurveyBaseDao {
	public static final String collectionName = "ethe_proxy_log";

	public ProxyLogDao() {
		setCollectionName(collectionName);
	}

	public void storeNewRequest(String action, JsonObject request, JsonObject response) {
		this.saveDocument(new JsonObject().put(FieldName.ACTION, action).put(FieldName.MESSAGEREQUEST, request)
				.put(FieldName.MESSSAGERESPONSE, response));
	}
}
