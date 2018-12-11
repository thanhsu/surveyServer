package com.survey.internal.action;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import com.survey.dbservice.dao.CashTransactionDao;
import com.survey.utils.CodeMapping;
import com.survey.utils.FieldName;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class CashTransferEnquiryAction extends InternalSurveyBaseAction {

	@Override
	public void doProccess() {
		String username = getMessageBody().getString(FieldName.USERNAME);
		Date from;
		Date to;
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
		try {
			to = sdf.parse(getMessageBody().getString(FieldName.TODATE) + " 23:59:59");
		} catch (ParseException e) {
			to = new Date();
			e.printStackTrace();
		}

		try {
			from = sdf.parse(getMessageBody().getString(FieldName.FROMDATE) + " 00:00:00");
		} catch (ParseException e) {
			from = to;
			from.setDate(to.getDate() - 7);
			e.printStackTrace();
		}
		// retrieve cash transfer in

		CashTransactionDao lvCashTransactionDao = new CashTransactionDao();
		Future<JsonArray> listCashTransferIn = Future.future();
		Future<JsonArray> listCashTransferOut = Future.future();
		CompositeFuture lvCompositeFuture = CompositeFuture.all(listCashTransferIn, listCashTransferOut);

		lvCashTransactionDao.retrieveListCashTransferIn(username, from.getTime(), to.getTime(), listCashTransferIn);
		CashTransactionDao lvCashTransactionDao2 = new CashTransactionDao();
		lvCashTransactionDao2.retrieveListCashTransferOut(username, from.getTime(), to.getTime(), listCashTransferOut);

		lvCompositeFuture.setHandler(arg0 -> {
			JsonObject data = new JsonObject();
			if (arg0.succeeded()) {
				if (listCashTransferIn.succeeded()) {
					data.put(FieldName.CASHTRANSFERIN, listCashTransferIn.result());
				} else {
					data.put(FieldName.CASHTRANSFERIN, new JsonArray());
				}

				if (listCashTransferOut.succeeded()) {
					data.put(FieldName.CASHTRANSFEROUT, listCashTransferOut.result());
				} else {
					data.put(FieldName.CASHTRANSFEROUT, new JsonArray());
				}
			} else {
				data = new JsonObject().put(FieldName.CASHTRANSFERIN, new JsonArray()).put(FieldName.CASHTRANSFEROUT,
						new JsonArray());
			}
			this.CompleteGenerateResponse(CodeMapping.C0000.name(), CodeMapping.C0000.value(), data, response);

		});

	}

}
