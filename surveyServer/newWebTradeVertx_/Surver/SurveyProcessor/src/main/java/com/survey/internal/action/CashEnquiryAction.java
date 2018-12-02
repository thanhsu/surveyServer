package com.survey.internal.action;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.survey.dbservice.dao.CashDepositDao;
import com.survey.dbservice.dao.CashWithdrawDao;
import com.survey.utils.CodeMapping;
import com.survey.utils.FieldName;
import com.survey.utils.MessageDefault;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

public class CashEnquiryAction extends InternalSurveyBaseAction {

	@Override
	public void doProccess() {
		String userid = getMessageBody().getString(FieldName.USERID);
		String username = getMessageBody().getString(FieldName.USERNAME);
		CashDepositDao lvCashDepositDao = new CashDepositDao();
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
		Date from;
		try {
			from = sdf.parse(getMessageBody().getString(FieldName.FROMDATE) + " 00:00:00");
			Date to = sdf.parse(getMessageBody().getString(FieldName.TODATE) + " 23:59:59");
			Future<JsonObject> lvListCashDeposit = lvCashDepositDao.retrieveAllDeposit(from.getTime(), to.getTime(),
					username);
			CashWithdrawDao lvCashWithdrawDao = new CashWithdrawDao();
			Future<JsonObject> lvCashWithDraw = lvCashWithdrawDao.retrieveAllWithdraw(from.getTime(), to.getTime(),
					username);
			
			CompositeFuture lvCompositeFuture = CompositeFuture.all(lvCashWithDraw, lvListCashDeposit);
			lvCompositeFuture.setHandler(handler -> {
				if (handler.succeeded()) {
					JsonObject resp = new JsonObject();
					resp.put("listDeposit", lvListCashDeposit.result());
					resp.put("listWithdraw", lvCashWithDraw.result());
					this.CompleteGenerateResponse(CodeMapping.C0000.toString(), "", resp, response);
				} else {
					response.complete(MessageDefault.RequestFailed(handler.cause().getMessage()));
				}
			});
		} catch (ParseException e) {
			response.complete(MessageDefault.ParamError(e.getMessage()));
		}
	}

}
