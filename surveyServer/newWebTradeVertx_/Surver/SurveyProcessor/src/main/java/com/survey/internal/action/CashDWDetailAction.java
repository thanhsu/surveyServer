package com.survey.internal.action;

import com.survey.dbservice.dao.CashDepositDao;
import com.survey.dbservice.dao.CashWithdrawDao;
import com.survey.utils.CodeMapping;
import com.survey.utils.FieldName;

public class CashDWDetailAction extends InternalSurveyBaseAction {

	@Override
	public void doProccess() {
		String dw= getMessageBody().getString(FieldName.DW);
		String transID = getMessageBody().getString(FieldName.TRANID);
		if(dw.equals("D")) {
			CashDepositDao lvCashDepositDao = new CashDepositDao();
			lvCashDepositDao.findOneByID(transID).setHandler(handler->{
				if(handler.result()!=null) {
					this.CompleteGenerateResponse(CodeMapping.C0000.name(), "ok", handler.result(), response);
				}else {
					this.CompleteGenerateResponse(CodeMapping.C1111.name(), "Not found", null, response);
				}
			});
		}else {
			CashWithdrawDao lvCashWithdrawDao = new CashWithdrawDao();
			lvCashWithdrawDao.findOneByID(transID).setHandler(handler->{
				if(handler.result()!=null) {
					this.CompleteGenerateResponse(CodeMapping.C0000.name(), "ok", handler.result(), response);
				}else {
					this.CompleteGenerateResponse(CodeMapping.C1111.name(), "Not found", null, response);
				}
			});
		}
		
	}

}
