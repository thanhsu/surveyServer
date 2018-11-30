package com.survey.notification.actions;

import com.survey.constant.UserNotificationEnum;
import com.survey.dbservice.dao.CashWithdrawDao;
import com.survey.utils.PushMessageBean;

public class NotifiCashWithdraw extends BaseSurveyNotification {
	private String withdrawID;

	public NotifiCashWithdraw(String pWithdrawID) {
		withdrawID = pWithdrawID;
	}

	@Override
	public void generate() {
		CashWithdrawDao lvCashWithdrawDao = new CashWithdrawDao();
		lvCashWithdrawDao.findOneByID(withdrawID).setHandler(h->{
			if(h.succeeded()&&h.result()!=null) {
				message = new PushMessageBean();
				message.setData(h.result());
				message.setType(UserNotificationEnum.CASHWITHDRAW);
				message.setDescription(UserNotificationEnum.CASHWITHDRAW.getDescription());
				this.setPrivate(true);
				this.setPublic(false);
				doSend();
			}
		});
	}

}
