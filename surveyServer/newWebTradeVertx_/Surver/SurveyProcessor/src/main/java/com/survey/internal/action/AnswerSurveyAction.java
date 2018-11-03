package com.survey.internal.action;

import com.survey.constant.EventBusDiscoveryConst;
import com.survey.dbservice.dao.ProxyLogDao;
import com.survey.dbservice.dao.SurveySubmitDao;
import com.survey.utils.CodeMapping;
import com.survey.utils.FieldName;
import com.survey.utils.Log;
import com.survey.utils.MessageDefault;
import com.survey.utils.VertxServiceCenter;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

public class AnswerSurveyAction extends InternalSurveyBaseAction {

	@Override
	public void doProccess() {
		String surveyID = getMessageBody().getString(FieldName.SURVEYID);
		String username = getMessageBody().getString(FieldName.USERNAME);
		JsonObject answerdata = getMessageBody().getJsonObject(FieldName.DATA);
		SurveySubmitDao lvSurveySubmitDao = new SurveySubmitDao();
		lvSurveySubmitDao.newSurveyResult(username, answerdata, surveyID).setHandler(id -> {
			if (id.succeeded()) {
				this.CompleteGenerateResponse(CodeMapping.C0000.toString(), CodeMapping.C0000.value(), null, response);
				Future<JsonObject> lv = Future.future();
				VertxServiceCenter.getInstance().sendNewMessage(EventBusDiscoveryConst.ETHEREUMPROXYDISCOVERY.name(),
						getMessageBody(), lv);
				lv.setHandler(handler -> {
					if (handler.succeeded()) {
						ProxyLogDao lvDao = new ProxyLogDao();
						lvDao.storeNewRequest(getMessageBody().getString(FieldName.ACTION), getMessageBody(),
								handler.result());
					} else {
						Log.print("[Submit Answer surveyData] Send Proxy Failed. Cause:" + handler.cause().getMessage(),
								Log.ERROR_LOG);
					}
				});
			} else {
				response.complete(MessageDefault.RequestFailed(id.cause().getMessage()));
			}
		});

	}

}
