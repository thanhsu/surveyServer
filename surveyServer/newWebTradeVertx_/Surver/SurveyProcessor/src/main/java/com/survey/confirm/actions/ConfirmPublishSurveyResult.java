package com.survey.confirm.actions;

import com.survey.dbservice.dao.CashWithdrawDao;
import com.survey.dbservice.dao.SurveyDao;
import com.survey.dbservice.dao.SurveyPushlishDao;
import com.survey.notification.actions.NotifiSurveyPushlished;
import com.survey.utils.FieldName;
import com.survey.utils.Log;

import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;

public class ConfirmPublishSurveyResult extends BaseConfirmAction {

	@Override
	public void doProcess(JsonObject msg) {
		boolean success = msg.getBoolean(FieldName.SUCCESS);
		String pushlishID = msg.getString(FieldName.PUSHLISHID);
		JsonObject data = new JsonObject();

		SurveyPushlishDao lvSurveyPushlishDao = new SurveyPushlishDao();
		lvSurveyPushlishDao.retrievePushlishByID(pushlishID).setHandler(h -> {
			if (h.succeeded() && h.result() != null) {
				String lvSurveyID = h.result().getString(FieldName.SURVEYID);
				String tranID = h.result().getString(FieldName.TRANID);
				
				CashWithdrawDao lvCashWithdrawDao = new CashWithdrawDao();
				lvCashWithdrawDao.updateSettlesStatus(tranID, success?"S":"U", "", "","");
				SurveyDao lvDao = new SurveyDao();
				if (success) {
					data.put(FieldName.STATUS, "N");
				} else {
					data.put(FieldName.STATUS, "L");
					data.put(FieldName.PUSHLISHREJECTCAUSE, msg.getString(FieldName.MESSAGE));
				}
				lvDao.updateSurveyData(lvSurveyID, data);
				lvDao.getMvFutureResponse().setHandler(handler->{
					if (success) {
						NotifiSurveyPushlished lvPushlished = new NotifiSurveyPushlished(lvSurveyID);
						lvPushlished.setPrivate(true);
						lvPushlished.setPublic(false);
						lvPushlished.generate();
					} else {
						NotifiSurveyPushlished lvPushlished = new NotifiSurveyPushlished(lvSurveyID);
						lvPushlished.setPrivate(true);
						lvPushlished.setPublic(false);
						lvPushlished.generate();
					}
				});
				
			} else {
				Log.println("Received invalid pushlishID. Message: " + Json.encode(msg), Log.ACCESS_LOG);
			}
		});

	}

}
