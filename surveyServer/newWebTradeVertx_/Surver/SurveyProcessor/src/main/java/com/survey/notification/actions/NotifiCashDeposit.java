package com.survey.notification.actions;

import com.survey.constant.UserNotificationEnum;
import com.survey.dbservice.dao.CashDepositDao;
import com.survey.utils.FieldName;
import com.survey.utils.PushMessageBean;

import io.vertx.core.json.JsonObject;

public class NotifiCashDeposit extends BaseSurveyNotification {
	private String depositId;

	public NotifiCashDeposit(String pDepositID) {
		depositId = pDepositID;
	}

	@Override
	public void generate() {
		CashDepositDao lvCashDepositDao = new CashDepositDao();
		lvCashDepositDao.queryDocument(new JsonObject().put(FieldName._ID, depositId), handler->{
			if(handler.succeeded()&&handler.result()!=null) {
				if(!handler.result().isEmpty()) {
					message = new PushMessageBean();
					message.setData(handler.result().get(0));
					message.setType(UserNotificationEnum.CASHDEPOSIT);
					message.setDescription(UserNotificationEnum.CASHDEPOSIT.getDescription());
					
					setPrivate(true);
					setPublic(false);
					doSend();
				}
			}
		});
		
		
	}

}
