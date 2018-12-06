package com.survey.internal.action;

import java.util.List;

import com.survey.dbservice.dao.CardDao;
import com.survey.utils.CodeMapping;
import com.survey.utils.FieldName;
import com.survey.utils.MessageDefault;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class CardCategoryAction extends InternalSurveyBaseAction {

	@Override
	public void doProccess() {
		String method = getMessageBody().getString(FieldName.METHOD);
		switch (method) {
		case "retrieve":
			CardDao lvCardDao = new CardDao();
			lvCardDao.getListCardCateGory("A", handler -> {
				List<JsonObject> rs = handler.result();
				if (rs != null) {
					this.CompleteGenerateResponse(CodeMapping.C0000.name(), CodeMapping.C0000.value(), rs, response);
				} else {
					this.CompleteGenerateResponse(CodeMapping.C1111.name(), CodeMapping.C1111.value(), null, response);
				}
			});
			break;
		case "add":
			CardDao lvCardDao2 = new CardDao();
			String categoryID = getMessageBody().getString(FieldName.CATEGORYID);
			String categoryName = getMessageBody().getString(FieldName.CATEGORY);
			String imageLink = getMessageBody().getString(FieldName.IMAGE);
			JsonArray lstVl = getMessageBody().getJsonArray(FieldName.LISTVALUE);
			lvCardDao2.addNewCategory(categoryID, categoryName, imageLink,lstVl);
			lvCardDao2.getMvFutureResponse().setHandler(handler -> {
				if (handler.succeeded()) {
					this.CompleteGenerateResponse(CodeMapping.C0000.name(), CodeMapping.C0000.value(), null, response);
				} else {
					this.CompleteGenerateResponse(CodeMapping.C1111.name(), CodeMapping.C1111.value(), null, response);
				}
			});
			break;
		case "disable":
			CardDao lvCardDao3 = new CardDao();
			String categoryID2 = getMessageBody().getString(FieldName.CATEGORYID);
			lvCardDao3.disableCardCategory(categoryID2);
			lvCardDao3.getMvFutureResponse().setHandler(handler->{
				response.complete(handler.result());
			});
			break;
		case "all":
			CardDao lvCardDao4 = new CardDao();
			lvCardDao4.getListCardCateGory("", handler -> {
				List<JsonObject> rs = handler.result();
				if (rs != null) {
					this.CompleteGenerateResponse(CodeMapping.C0000.name(), CodeMapping.C0000.value(), rs, response);
				} else {
					this.CompleteGenerateResponse(CodeMapping.C1111.name(), CodeMapping.C1111.value(), null, response);
				}
			});
			break;
		default:
			response.complete(MessageDefault.ActionNotFound());
			break;
		}
	}

}
