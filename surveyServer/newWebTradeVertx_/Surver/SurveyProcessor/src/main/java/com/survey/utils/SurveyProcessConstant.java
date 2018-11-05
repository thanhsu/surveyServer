package com.survey.utils;

import java.util.HashMap;
import java.util.Map;
import com.survey.confirm.actions.BaseConfirmAction;
import com.survey.confirm.actions.CreateUser;
import com.survey.internal.action.AccountBalanceProxyAction;
import com.survey.internal.action.ActiveUserAction;
import com.survey.internal.action.CancelCashAction;
import com.survey.internal.action.CashEnquiryAction;
import com.survey.internal.action.ChangePasswordAction;
import com.survey.internal.action.CheckPermissionAnswerSurveyAction;
import com.survey.internal.action.DepositAction;
import com.survey.internal.action.InternalSurveyBaseAction;
import com.survey.internal.action.LoginAction;
import com.survey.internal.action.NewSurveyAction;
import com.survey.internal.action.PaymentAction;
import com.survey.internal.action.PushlishSurveyAction;
import com.survey.internal.action.RegisterAction;
import com.survey.internal.action.ResetPasswordAction;
import com.survey.internal.action.RetrieveCashMethodAction;
import com.survey.internal.action.RetrieveConfigAction;
import com.survey.internal.action.RetrievePointValueAction;
import com.survey.internal.action.RetrieveSurveyAction;
import com.survey.internal.action.RetrieveUserinfoAction;
import com.survey.internal.action.UpdateSurveyDataAction;
import com.survey.internal.action.UpdateUserInfoAction;
import com.survey.internal.action.WithdrawAction;

public class SurveyProcessConstant {
	private static SurveyProcessConstant instance;
	public static HashMap<String, BaseConfirmAction> confirmActionMapping = new HashMap<>();
	public static Map<String, InternalSurveyBaseAction> mvActionMapping = new HashMap<>();
	

	public static synchronized SurveyProcessConstant getInstance() {
		if (instance == null) {
			instance = new SurveyProcessConstant();
			instance.init();
			instance.initActionMapping();
		}
		return instance;
	}

	public void init() {
		confirmActionMapping.put("create_account", new CreateUser());
	}

	private void initActionMapping() {
		mvActionMapping.put("login", new LoginAction());
		mvActionMapping.put("register", new RegisterAction());
		mvActionMapping.put("activeaccount", new ActiveUserAction());
		mvActionMapping.put("changepassword", new ChangePasswordAction());
		mvActionMapping.put("resetpassword", new ResetPasswordAction());
		mvActionMapping.put("userinfo", new RetrieveUserinfoAction());
		mvActionMapping.put("updateuserinfo", new UpdateUserInfoAction());

		// 2
		mvActionMapping.put("retrieveconfig", new RetrieveConfigAction());
		// 3
		mvActionMapping.put("accountbalance", new AccountBalanceProxyAction());
		mvActionMapping.put("pointvalue", new RetrievePointValueAction());
		mvActionMapping.put("cashmethod", new RetrieveCashMethodAction());
		//
		mvActionMapping.put("deposit", new DepositAction());
		mvActionMapping.put("withdraw", new WithdrawAction());
		mvActionMapping.put("cashenquiry", new CashEnquiryAction());
		mvActionMapping.put("cancelcash", new CancelCashAction());
		mvActionMapping.put("payment", new PaymentAction());
		// 4
		mvActionMapping.put("retrievesurvey", new RetrieveSurveyAction());

		mvActionMapping.put("newsurvey", new NewSurveyAction());
		mvActionMapping.put("updatesurvey", new UpdateSurveyDataAction());
		mvActionMapping.put("pushlish", new PushlishSurveyAction());
		mvActionMapping.put("checkanswerpermission", new CheckPermissionAnswerSurveyAction());

	}

	public InternalSurveyBaseAction getInternalAction(String p) {
		return mvActionMapping.get(p);
	}

	public BaseConfirmAction getConfirmAction(String p) {
		return confirmActionMapping.get(p);
	}
}