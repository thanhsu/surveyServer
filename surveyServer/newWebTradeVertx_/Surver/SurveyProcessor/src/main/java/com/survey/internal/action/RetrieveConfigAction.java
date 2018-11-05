package com.survey.internal.action;

import com.survey.dbservice.dao.UtilsDao;
import com.survey.utils.CodeMapping;
import com.survey.utils.FieldName;
import com.survey.utils.MessageDefault;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

public class RetrieveConfigAction extends InternalSurveyBaseAction {

	@Override
	public void doProccess() {
		String method = getMessageBody().getString(FieldName.METHOD);
		if (method != null) {
			UtilsDao lvUtils = new UtilsDao();
			switch (method) {
			case FieldName.CATEGORY:
				// lvUtils = new UtilsDao();
				lvUtils.retrieveAllCategory(new JsonObject()).setHandler(handler -> {
					this.CompleteGenerateResponse(CodeMapping.C0000.toString(), CodeMapping.C0000.value(),
							handler.result().getJsonArray("data"), response);
				});
				break;
			case FieldName.RULE:
				// lvUtils = new UtilsDao();
				lvUtils.retrieveAllRules().setHandler(handler -> {
					this.CompleteGenerateResponse(CodeMapping.C0000.toString(), CodeMapping.C0000.value(),
							handler.result().getJsonArray("data"), response);
				});
				break;

			case FieldName.CAREER:
				// lvUtils = new UtilsDao();
				lvUtils.retrieveAllCareer().setHandler(handler -> {
					this.CompleteGenerateResponse(CodeMapping.C0000.toString(), CodeMapping.C0000.value(),
							handler.result().getJsonArray("data"), response);
				});

				break;

			default:
				Future<JsonObject> lvRules1 = lvUtils.retrieveAllRules();
				lvUtils = new UtilsDao();
				Future<JsonObject> lvCategory1 = lvUtils.retrieveAllCategory(new JsonObject());
				lvUtils = new UtilsDao();
				Future<JsonObject> lvCareer1 = lvUtils.retrieveAllCareer();
				CompositeFuture compFuture = CompositeFuture.all(lvRules1, lvCategory1, lvCareer1);
				compFuture.setHandler(handler -> {
					if (handler.succeeded()) {
						JsonObject rspData = new JsonObject();
						rspData.put("rules", lvRules1.result());
						rspData.put("category", lvCategory1.result());
						rspData.put("career", lvCareer1.result());
						this.CompleteGenerateResponse(CodeMapping.C0000.toString(), CodeMapping.C0000.value(), rspData,
								response);
					} else {
						response.complete(MessageDefault.RequestFailed(handler.cause().getMessage()));
					}
				});
				break;
			}
		} else {
			UtilsDao lvUtils = new UtilsDao();
			Future<JsonObject> lvRules = lvUtils.retrieveAllRules();
			lvUtils = new UtilsDao();
			Future<JsonObject> lvCategory = lvUtils.retrieveAllCategory(new JsonObject());
			lvUtils = new UtilsDao();
			Future<JsonObject> lvCareer = lvUtils.retrieveAllCareer();
			CompositeFuture compFuture = CompositeFuture.all(lvRules, lvCategory, lvCareer);
			compFuture.setHandler(handler -> {
				if (handler.succeeded()) {
					JsonObject rspData = new JsonObject();
					rspData.put("rules", lvRules.result());
					rspData.put("category", lvCategory.result());
					rspData.put("career", lvCareer.result());
					this.CompleteGenerateResponse(CodeMapping.C0000.toString(), CodeMapping.C0000.value(), rspData,
							response);
				} else {
					response.complete(MessageDefault.RequestFailed(handler.cause().getMessage()));
				}
			});
		}
		// lvRules.setHandler(handler -> {
		// JsonObject data = handler.result();
		// if (data == null) {
		// data = new JsonObject();
		// }
		// JsonObject rspData = response.result();
		// if (rspData == null) {
		// rspData = new JsonObject();
		// }
		// response.map(rspData.put("rules", data));
		// });
		// lvCategory.setHandler(handler -> {
		// JsonObject data = handler.result();
		// if (data == null) {
		// data = new JsonObject();
		// }
		// JsonObject rspData = response.result();
		// if (rspData == null) {
		// rspData = new JsonObject();
		// }
		// response.map(rspData.put("category", data));
		// });
	}

}
