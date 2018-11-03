package com.survey.dbservice.dao;

import com.survey.utils.FieldName;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

public class SurveyPushlishDao extends SurveyBaseDao {
	public static final String collectionname = "surveypushlish";

	public SurveyPushlishDao() {
		setCollectionName(collectionname);
	}

	public Future<String> newPushlishAction(String surveyID, double limitResp, float pointPerOne, float initialFund,
			boolean noti, float limitFund) {
		return this.saveDocumentReturnID(
				new JsonObject().put(FieldName.SURVEYID, surveyID).put(FieldName.INITIALFUND, String.valueOf(initialFund))
						.put(FieldName.LIMITFUND, String.valueOf( limitFund)).put(FieldName.LIMITRESPONSE, String.valueOf( limitResp))
						.put(FieldName.PAYOUT, String.valueOf(pointPerOne)).put(FieldName.NOTIFY, noti));

	}

}
