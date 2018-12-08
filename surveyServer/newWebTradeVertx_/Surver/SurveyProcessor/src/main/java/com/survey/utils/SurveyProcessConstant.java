package com.survey.utils;

import java.util.HashMap;
import java.util.Map;
import com.survey.confirm.actions.BaseConfirmAction;
import com.survey.confirm.actions.ConfirmCreateUser;
import com.survey.confirm.actions.ConfirmPublishSurveyResult;
import com.survey.confirm.actions.ConfirmStopSurvey;
import com.survey.confirm.actions.ConfirmSurveyAnswer;
import com.survey.confirm.actions.ConfirmSurveyDeposit;
import com.survey.confirm.actions.ConfirmSurveyWithdraw;
import com.survey.confirm.actions.ConfirmTransaction;
import com.survey.dbservice.dao.CreateNewBank;
import com.survey.internal.action.AccountBalanceProxyAction;
import com.survey.internal.action.ActiveUserAction;
import com.survey.internal.action.AddFavouriteSurveyAction;
import com.survey.internal.action.AnswerSurveyAction;
import com.survey.internal.action.CancelCashAction;
import com.survey.internal.action.CardCategoryAction;
import com.survey.internal.action.CardDataAction;
import com.survey.internal.action.CashEnquiryAction;
import com.survey.internal.action.CashManualDeposit;
import com.survey.internal.action.CashTranserAction;
import com.survey.internal.action.CashTransferEnquiryAction;
import com.survey.internal.action.ChangePasswordAction;
import com.survey.internal.action.CheckPermissionAnswerSurveyAction;
import com.survey.internal.action.CopySurveyAction;
import com.survey.internal.action.DeleteSurveyAction;
import com.survey.internal.action.DepositAction;
import com.survey.internal.action.DepositWithdrawSurveyAction;
import com.survey.internal.action.DisableSurveyAction;
import com.survey.internal.action.EtheTransactionHistory;
import com.survey.internal.action.InternalSurveyBaseAction;
import com.survey.internal.action.LoginAction;
import com.survey.internal.action.NewSurveyAction;
import com.survey.internal.action.PaymentAction;
import com.survey.internal.action.PushlishSurveyAction;
import com.survey.internal.action.RegisterAction;
import com.survey.internal.action.RemoveFavouriteSurveyAction;
import com.survey.internal.action.RenewPINAction;
import com.survey.internal.action.ResetPasswordAction;
import com.survey.internal.action.RestoreSurveyAction;
import com.survey.internal.action.RetrieveAccountBaseInfo;
import com.survey.internal.action.RetrieveCashMethodAction;
import com.survey.internal.action.RetrieveConfigAction;
import com.survey.internal.action.RetrievePointValueAction;
import com.survey.internal.action.RetrieveSurveyAction;
import com.survey.internal.action.RetrieveSurveyAnsweredAction;
import com.survey.internal.action.RetrieveSurveyBalanceAction;
import com.survey.internal.action.RetrieveSurveyBaseInfoAction;
import com.survey.internal.action.RetrieveUserNotificationAction;
import com.survey.internal.action.RetrieveUserinfoAction;
import com.survey.internal.action.StopPushlishAction;
import com.survey.internal.action.Testing;
import com.survey.internal.action.UpdateSurveyDataAction;
import com.survey.internal.action.UpdateUserInfoAction;
import com.survey.internal.action.VerifyPINAction;
import com.survey.internal.action.WithdrawAction;

public class SurveyProcessConstant {
	private static SurveyProcessConstant instance;
	public static HashMap<String, BaseConfirmAction> confirmActionMapping = new HashMap<>();
	public static Map<String, InternalSurveyBaseAction> mvActionMapping = new HashMap<>();
	

	public static synchronized SurveyProcessConstant getInstance() {
		if (instance == null) {
			instance = new SurveyProcessConstant();
			instance.initConfirmAction();
			instance.initActionMapping();
		}
		return instance;
	}

	public void initConfirmAction() {
		confirmActionMapping.put("create_account", new ConfirmCreateUser());
		confirmActionMapping.put("pushlish_survey", new ConfirmPublishSurveyResult());
		confirmActionMapping.put("stop_survey", new ConfirmStopSurvey());
		confirmActionMapping.put("survey_deposit", new ConfirmSurveyDeposit());
		confirmActionMapping.put("survey_withdraw", new ConfirmSurveyWithdraw());
		confirmActionMapping.put("survey_answer", new ConfirmSurveyAnswer());
		confirmActionMapping.put("cashtransfer", new ConfirmTransaction());
		
	}

	private void initActionMapping() {
		mvActionMapping.put("testing", new Testing());
		mvActionMapping.put("login", new LoginAction());
		mvActionMapping.put("register", new RegisterAction());
		mvActionMapping.put("activeaccount", new ActiveUserAction());
		mvActionMapping.put("changepassword", new ChangePasswordAction());
		mvActionMapping.put("resetpassword", new ResetPasswordAction());
		mvActionMapping.put("userinfo", new RetrieveUserinfoAction());
		mvActionMapping.put("updateuserinfo", new UpdateUserInfoAction());
		mvActionMapping.put("verifypin", new VerifyPINAction());
		mvActionMapping.put("newpin",new RenewPINAction());

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
		mvActionMapping.put("manualdeposit", new CashManualDeposit());
		
		//-- Cash transfer
		mvActionMapping.put("cashtransfer", new CashTranserAction());
		mvActionMapping.put("verifyuser", new RetrieveAccountBaseInfo());
		mvActionMapping.put("cashtransferenquiry", new CashTransferEnquiryAction());
		mvActionMapping.put("ethetranshistory", new EtheTransactionHistory());
		// 4
		mvActionMapping.put("retrievesurvey", new RetrieveSurveyAction());
		mvActionMapping.put("retrievesurveybaseinfo", new RetrieveSurveyBaseInfoAction());
		mvActionMapping.put("copysurvey", new CopySurveyAction());
		mvActionMapping.put("newsurvey", new NewSurveyAction());
		mvActionMapping.put("updatesurvey", new UpdateSurveyDataAction());
		mvActionMapping.put("pushlish", new PushlishSurveyAction());
		mvActionMapping.put("checkanswerpermission", new CheckPermissionAnswerSurveyAction());
		mvActionMapping.put("answer", new AnswerSurveyAction());
		mvActionMapping.put("disablesurvey", new DisableSurveyAction());
		mvActionMapping.put("deletesurvey", new DeleteSurveyAction());
		mvActionMapping.put("restoresurvey", new RestoreSurveyAction());
		mvActionMapping.put("addfavouritesurvey", new AddFavouriteSurveyAction());
		mvActionMapping.put("removefavouritesurvey",new RemoveFavouriteSurveyAction());
		mvActionMapping.put("retrievesurveyresponse", new RetrieveSurveyAnsweredAction());
		mvActionMapping.put("surveybalance", new RetrieveSurveyBalanceAction());
		mvActionMapping.put("surveydw", new DepositWithdrawSurveyAction());
		mvActionMapping.put("stopsurvey", new StopPushlishAction());
		//5
		mvActionMapping.put("addbank",new CreateNewBank());
		mvActionMapping.put("retrieveusernotification", new RetrieveUserNotificationAction());
		
		
		//Card action
		mvActionMapping.put("cardcategory", new CardCategoryAction());
		mvActionMapping.put("carddata", new CardDataAction());
		
	}

	public InternalSurveyBaseAction getInternalAction(String p) {
		return mvActionMapping.get(p);
	}

	public BaseConfirmAction getConfirmAction(String p) {
		return confirmActionMapping.get(p);
	}
}
