package com.survey.internal.action;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import com.survey.dbservice.dao.CashDepositDao;
import com.survey.dbservice.dao.ProxyLogDao;
import com.survey.etheaction.ProxyCashDepositWithSystem;
import com.survey.notification.actions.NotifiCashDeposit;
import com.survey.utils.CodeMapping;
import com.survey.utils.FieldName;
import com.survey.utils.Log;
import com.survey.utils.RSAEncrypt;

public class ConfirmCashAction extends InternalSurveyBaseAction {

	@Override
	public void doProccess() {
		String result = getMessageBody().getString(FieldName.RESULT);
		String privateToken = getMessageBody().getString(FieldName.TOKEN);
		//String method = getMessageBody().getString(FieldName.METHOD);
		String transID = getMessageBody().getString("i");
		CashDepositDao lvCashDepositDao = new CashDepositDao();
		lvCashDepositDao.findOneByID(transID).setHandler(handler -> {
			if (handler.result() == null) {
				Log.print("Error when confirm deposit value: " + getMessageBody().toString(), Log.ERROR_LOG);
				return;
			}
			/*if (!checkToken(handler.result().getString(FieldName.USERID), privateToken)) {
				this.CompleteGenerateResponse(CodeMapping.C1111.name(), "link invalid", null, response);
				return;
			}*/

			if (result.equals("approval")) {
				ProxyCashDepositWithSystem lvProxyCashDepositWithSystem = new ProxyCashDepositWithSystem();
				lvProxyCashDepositWithSystem.setAmount(handler.result().getValue(FieldName.AMOUNT).toString());
				lvProxyCashDepositWithSystem.setFromuser(handler.result().getString(FieldName.USERNAME));
				lvProxyCashDepositWithSystem.setTransid(transID);
				lvProxyCashDepositWithSystem.sendToProxyServer().setHandler(h -> {
					if (!h.result().getString(FieldName.CODE).equals("P0000")) {
						Log.print("Deposit to user error!" + h.result().toString(), Log.ERROR_LOG);
					}
					ProxyLogDao lvProxyLogDao = new ProxyLogDao();
					lvProxyLogDao.saveDocument(h.result());
				});
			} else if (result.equals("cancel")) {
				CashDepositDao lvCashDepositDao2 = new CashDepositDao();
				lvCashDepositDao2.updateSettlesStatus(transID, "U", "Reject in paument", 0);
				NotifiCashDeposit lvNotifiCashDeposit = new NotifiCashDeposit(transID);
				lvNotifiCashDeposit.generate();
			}
			this.CompleteGenerateResponse(CodeMapping.C0000.name(), "Wait confirm", null, response);
		});

	}

	private boolean checkToken(String userID, String privateToken) {
		String decode;
		try {
			decode = RSAEncrypt.getIntance().decrypt(privateToken);
			return decode.equals(userID);
		} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException
				| BadPaddingException | UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
}
