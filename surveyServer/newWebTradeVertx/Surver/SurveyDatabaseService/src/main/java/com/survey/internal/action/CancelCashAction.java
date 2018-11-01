package com.survey.internal.action;

import com.squareup.okhttp.internal.framed.FrameReader.Handler;
import com.survey.dbservice.dao.CashDepositDao;
import com.survey.dbservice.dao.CashWithdrawDao;
import com.survey.utils.CodeMapping;
import com.survey.utils.FieldName;
import com.survey.utils.MessageDefault;

public class CancelCashAction extends InternalSurveyBaseAction {
	@Override
	public void doProccess() {
		String tranID = getMessageBody().getString(FieldName.TRANID);
		String userID = getMessageBody().getString(FieldName.USERID);

		String dw = getMessageBody().getString("dw");
		if (dw.equals("W")) {
			CashWithdrawDao lvCashWithdrawDao = new CashWithdrawDao();
			lvCashWithdrawDao.findOneByID(tranID).setHandler(handler -> {
				if (handler.succeeded() && handler.result() != null) {
					if (handler.result().getString(FieldName.USERID).equals(userID)) {
						String settleStaus = handler.result().getString(FieldName.STATUS);
						if (settleStaus.equals("S") || settleStaus.equals("R") || settleStaus.equals("C")) {
							this.CompleteGenerateResponse(CodeMapping.W2222.toString(), CodeMapping.W2222.value(),
									handler.result(), response);
						} else {
							lvCashWithdrawDao.cancelWithdraw(tranID, userID);
							lvCashWithdrawDao.getMvFutureResponse().setHandler(handler2 -> {
								response.complete(handler2.result());
							});
						}
					} else {
						response.complete(MessageDefault.PermissionError());
					}
				} else {
					response.complete(MessageDefault.RequestFailed("Transaction not found!"));
				}
			});
		} else if (dw.equals("D")) {
			CashDepositDao lvCashDepositDao = new CashDepositDao();
			lvCashDepositDao.findOneByID(tranID).setHandler(handler -> {
				if (handler.succeeded() && handler.result() != null) {
					if (handler.result().getString(FieldName.USERID).equals(userID)) {
						String settleStaus = handler.result().getString(FieldName.STATUS);
						if (settleStaus.equals("S") || settleStaus.equals("R") || settleStaus.equals("C")) {
							this.CompleteGenerateResponse(CodeMapping.W2222.toString(), CodeMapping.W2222.value(),
									handler.result(), response);
						} else {
							lvCashDepositDao.cancelDeposit(tranID, userID);
							lvCashDepositDao.getMvFutureResponse().setHandler(handler2 -> {
								response.complete(handler2.result());
							});
						}
					} else {
						response.complete(MessageDefault.PermissionError());
					}
				} else {
					response.complete(MessageDefault.RequestFailed("Transaction not found!"));
				}
			});
		} else {
			response.complete(MessageDefault.ParamError("dw value wrong!"));
		}
	}

}
