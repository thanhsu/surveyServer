package com.survey.dbservice.dao;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.sql.Date;
import java.sql.Timestamp;

import com.survey.constant.EventBusDiscoveryConst;
import com.survey.otp.OTPManagerException;
import com.survey.utils.CodeMapping;
import com.survey.utils.Encrypt;
import com.survey.utils.FieldName;
import com.survey.utils.GoogleUserBean;
import com.survey.utils.SurveyGoogleApiUtils;
import com.survey.utils.SurveyToken;
import com.survey.utils.Utils;
import com.survey.utils.VertxServiceCenter;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.UpdateOptions;

public class UserDao extends SurveyBaseDao {
	private static int maxFaild = 5;
	private static final String UserCollectionName = "user";

	public UserDao() {
		setCollectionName(UserCollectionName);
	}

	public Future<JsonObject> doLogin(String username, String password) {
		mvFutureResponse = Future.future();
		JsonObject query = new JsonObject().put(FieldName.USERNAME, username);
		this.queryDocument(query, handler -> {
			if (handler.succeeded() && handler.result() != null) {
				if (handler.result().isEmpty() || handler.result().get(0) == null) {
					this.CompleteGenerateResponse(CodeMapping.U1111.toString(), CodeMapping.U1111.value(), null);
				} else {
					JsonObject userData = handler.result().get(0);
					if (userData.getString(FieldName.STATUS).equals("D")) {
						this.CompleteGenerateResponse(CodeMapping.U3333.toString(), CodeMapping.U3333.value(), null);
						return;
					}
					if (Encrypt.compare(password, userData.getString(FieldName.PASSWORD))) {
						userData.remove(FieldName.PASSWORD);
						userData.remove(FieldName.PIN);
						userData.put(FieldName.NEEDCHANGEPASSWORD, false);
						userData.remove(FieldName.TEMPPASSWORD);
						userData.remove(FieldName.EXPIREDTIME);
						this.CompleteGenerateResponse(CodeMapping.C0000.toString(), "Login is success", userData);
						loginSuccess(userData.getString("_id"));
					}
					if (userData.getString(FieldName.TEMPPASSWORD) != null
							&& userData.getLong(FieldName.EXPIREDTIME) != null) {
						if (password.equals(userData.getString(FieldName.TEMPPASSWORD))) {
							long lvNow = new java.util.Date().getTime();
							long expired = userData.getLong(FieldName.EXPIREDTIME);
							if (expired >= lvNow) {
								userData.put(FieldName.NEEDCHANGEPASSWORD, true);
								userData.remove(FieldName.TEMPPASSWORD);
								userData.remove(FieldName.EXPIREDTIME);
								this.CompleteGenerateResponse(CodeMapping.C0000.toString(), "Login is success",
										userData);
								String tmpPassword =userData.getString(FieldName.TEMPPASSWORD);
								String newPass =userData.getString(FieldName.TEMPPASSWORD);
								try {
									newPass = Encrypt.encode(tmpPassword);
								} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								this.updateDocument(new JsonObject().put(FieldName.USERNAME, username), 
										new JsonObject().put(FieldName.TEMPPASSWORD, "").put(FieldName.PASSWORD, newPass).put(FieldName.OLDPASSWORD,userData.getString(FieldName.PASSWORD) ), new UpdateOptions(false), h3->{});
							} else {
								this.CompleteGenerateResponse(CodeMapping.U2222.toString(), CodeMapping.U2222.value(),
										null);
								loginFaile(userData);
							}
						} else {
							this.CompleteGenerateResponse(CodeMapping.U2222.toString(), CodeMapping.U2222.value(),
									null);
							loginFaile(userData);
						}
					} else {
						this.CompleteGenerateResponse(CodeMapping.U2222.toString(), CodeMapping.U2222.value(), null);
						loginFaile(userData);
					}
				}
			} else {
				getMvFutureResponse().fail("Username not Found");
				getMvFutureResponse().complete();
			}
		});
		return mvFutureResponse;
	}

	public void storeTempPassword(String username, String tempPassword, java.util.Date expiredTime) {
		JsonObject tmpPassword = new JsonObject().put(FieldName.TEMPPASSWORD, tempPassword).put(FieldName.EXPIREDTIME,
				expiredTime.getTime());
		this.updateDocument(new JsonObject().put(FieldName.USERNAME, username), tmpPassword, new UpdateOptions(false),
				handler -> {
					this.CompleteGenerateResponse(CodeMapping.C0000.toString(), "", null);
				});
	}

	public void loginByGoogle(String userToken) {
		this.queryDocument(new JsonObject().put(FieldName.GOOGLETOKEN, userToken), handler -> {
			if (handler.succeeded() && handler.result() != null) {
				if (!handler.result().isEmpty()) {
					JsonObject userData = handler.result().get(0);
					userData.remove(FieldName.PASSWORD);
					userData.remove(FieldName.PIN);
					this.CompleteGenerateResponse(CodeMapping.C0000.toString(), "Login is success", userData);
					loginSuccess(userData.getString("_id"));
				} else {
					// Create User
					createUserByGoogleID(userToken);
				}
			} else {
				// Create User
				createUserByGoogleID(userToken);
			}
		});
	}

	private void createUserByGoogleID(String token) {
		try {
			GoogleUserBean lvBean = SurveyGoogleApiUtils.getInstance().retrieveUserInfo(token);
			getClientIDbyEmail(lvBean.getEmail()).setHandler(r -> {
				if (r.succeeded() & r.result() != null) {
					if (r.result().getString(FieldName.CODE).equals(CodeMapping.U0000.toString())) {
						this.CompleteGenerateResponse(CodeMapping.R3333.toString(), CodeMapping.R3333.value(), null);
					} else {
						JsonObject query = new JsonObject().put(FieldName.USERNAME, lvBean.getEmail())
								.put(FieldName.GOOGLETOKEN, token).put(FieldName.AVATAR, lvBean.getAvatar());
						query.put(FieldName.PASSWORD, "").put(FieldName.FULLNAME, lvBean.getFullname())
								.put(FieldName.EMAIL, lvBean.getEmail()).put(FieldName.STATUS, "L")
								.put(FieldName.TOKEN, token);
						this.saveDocumentReturnID(query).setHandler(handler -> {
							this.doLogin(lvBean.getEmail(), "");
							Future<JsonObject> lvProxyResult = Future.future();
							JsonObject rq = new JsonObject().put(FieldName.ACTION, "createaccount")
									.put(FieldName.USERNAME, lvBean.getEmail()).put(FieldName.EMAIL, lvBean.getEmail());
							VertxServiceCenter.getInstance().sendNewMessage(
									EventBusDiscoveryConst.ETHEREUMPROXYDISCOVERY.name(), rq, lvProxyResult);
							lvProxyResult.setHandler(x -> {
								ProxyLogDao lvDao = new ProxyLogDao();
								lvDao.storeNewRequest("createaccount", rq, lvProxyResult.result());
							});
						});

					}
				} else {
					JsonObject query = new JsonObject().put(FieldName.USERNAME, lvBean.getEmail())
							.put(FieldName.GOOGLETOKEN, token).put(FieldName.AVATAR, lvBean.getAvatar());
					query.put(FieldName.PASSWORD, "").put(FieldName.FULLNAME, lvBean.getFullname())
							.put(FieldName.EMAIL, lvBean.getEmail()).put(FieldName.STATUS, "L")
							.put(FieldName.TOKEN, token);
					this.saveDocumentReturnID(query).setHandler(handler -> {
						this.doLogin(lvBean.getEmail(), "");
						Future<JsonObject> lvProxyResult = Future.future();
						JsonObject rq = new JsonObject().put(FieldName.ACTION, "createaccount")
								.put(FieldName.USERNAME, lvBean.getEmail()).put(FieldName.EMAIL, lvBean.getEmail());
						VertxServiceCenter.getInstance().sendNewMessage(
								EventBusDiscoveryConst.ETHEREUMPROXYDISCOVERY.name(), rq, lvProxyResult);
						lvProxyResult.setHandler(x -> {
							ProxyLogDao lvDao = new ProxyLogDao();
							lvDao.storeNewRequest("createaccount", rq, lvProxyResult.result());
						});
					});
				}
			});
		} catch (GeneralSecurityException | IOException e) {
			this.CompleteGenerateResponse(CodeMapping.U2222.toString(), CodeMapping.U2222.value(), null);
		}

	}

	public void doRegister(String username, String password, String email, String fullname) {
		mvFutureResponse = Future.future();
		JsonObject query = new JsonObject().put(FieldName.USERNAME, username);
		this.queryDocument(query, handler -> {
			if (handler.succeeded() && !handler.result().isEmpty()) {
				this.CompleteGenerateResponse(CodeMapping.R1111.toString(), CodeMapping.R1111.value(), null);
			} else {
				// Check Email is Exist
				getClientIDbyEmail(email).setHandler(r -> {
					if (r.succeeded() & r.result() != null) {
						if (r.result().getString(FieldName.CODE).equals(CodeMapping.U0000.toString())) {
							this.CompleteGenerateResponse(CodeMapping.R3333.toString(), CodeMapping.R3333.value(),
									null);
						} else {
							// Check Password
							String ecryptPassword = password;

							try {
								ecryptPassword = Encrypt.encode(password);

							} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
								e.printStackTrace();
							}
							String token = ecryptPassword;
							try {
								token = SurveyToken.getInstance().registerToken(username);
							} catch (OTPManagerException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							query.put(FieldName.PASSWORD, ecryptPassword).put(FieldName.FULLNAME, fullname)
									.put(FieldName.EMAIL, email).put(FieldName.STATUS, "L").put(FieldName.TOKEN, token);
							this.saveDocument(query);
							query.remove(FieldName.PASSWORD);
							this.CompleteGenerateResponse(CodeMapping.R0000.toString(), CodeMapping.R0000.value(),
									query);
						}
					} else {
						// Check Password
						String ecryptPassword = password;
						try {
							ecryptPassword = Encrypt.encode(password);
						} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
							e.printStackTrace();
						}
						query.put(FieldName.PASSWORD, ecryptPassword).put(FieldName.FULLNAME, fullname)
								.put(FieldName.EMAIL, email).put(FieldName.STATUS, "L");
						this.saveDocument(query);
						query.remove(FieldName.PASSWORD);
						this.CompleteGenerateResponse(CodeMapping.R0000.toString(), CodeMapping.R0000.value(), query);
					}
				});

				// // Check Password
				// String ecryptPassword = password;
				// try {
				// ecryptPassword = Encrypt.encode(password);
				// } catch (InstantiationException | IllegalAccessException |
				// ClassNotFoundException e) {
				// e.printStackTrace();
				// }
				// query.put(FieldName.PASSWORD, ecryptPassword).put(FieldName.FULLNAME,
				// fullname)
				// .put(FieldName.EMAIL, email).put(FieldName.STATUS, "L");
				// this.saveDocument(query);
				// query.remove(FieldName.PASSWORD);
				// this.CompleteGenerateResponse(CodeMapping.R0000.toString(),
				// CodeMapping.R0000.value(), query);
			}
		});
	}

	public void updateStatus(String username, String status) {
		updateDocument(new JsonObject().put(FieldName.USERNAME, username),
				new JsonObject().put(FieldName.STATUS, status), null, handler -> {
					if (handler.succeeded()) {
						this.CompleteGenerateResponse(CodeMapping.C0000.toString(), CodeMapping.C0000.value(), null);
					} else {
						this.CompleteGenerateResponse(CodeMapping.U000A.toString(), handler.cause().getMessage(), null);
					}
				});
	}

	public Future<JsonObject> getClientIDbyEmail(String email) {
		final Future<JsonObject> tmp = Future.future();
		BaseDaoConnection.getInstance().getMongoClient().findOne(UserCollectionName,
				new JsonObject().put(FieldName.EMAIL, email), null, handler -> {
					if (handler.succeeded() && handler.result() != null) {
						this.CompleteGenerateResponse(CodeMapping.U0000.toString(), "", handler.result(), tmp);

					} else {
						this.CompleteGenerateResponse(CodeMapping.U4444.toString(), CodeMapping.U4444.value(), null,
								tmp);
					}
				});
		return tmp;
	}

	public void doGetUserInfo(String userID) {
		JsonObject query = new JsonObject().put("_id", userID);
		this.queryDocument(query, handler -> {

			if (handler.succeeded() && handler.result() != null) {
				if (handler.result().isEmpty() || handler.result().get(0) == null) {
					this.CompleteGenerateResponse(CodeMapping.U1111.toString(), CodeMapping.U1111.value(), null);
				} else {
					JsonObject userData = handler.result().get(0);
					userData.remove(FieldName.PASSWORD);
					userData.remove(FieldName._ID);
					userData.remove(FieldName.TOKEN);
					this.CompleteGenerateResponse(CodeMapping.U0000.toString(), CodeMapping.U0000.value(), userData);
					return;
				}
			} else {
				getMvFutureResponse().fail("user id not found");
				getMvFutureResponse().complete();
			}

		});
	}

	public void doGetUserInfobyUserName(String username) {
		JsonObject query = new JsonObject().put(FieldName.USERNAME, username);
		this.queryDocument(query, handler -> {

			if (handler.succeeded() && handler.result() != null) {
				if (handler.result().isEmpty() || handler.result().get(0) == null) {
					this.CompleteGenerateResponse(CodeMapping.U1111.toString(), CodeMapping.U1111.value(), null);
				} else {
					JsonObject userData = handler.result().get(0);
					this.CompleteGenerateResponse(CodeMapping.U0000.toString(), CodeMapping.U0000.value(), userData);
					return;
				}
			} else {
				getMvFutureResponse().fail("user id not found");
			}

		});
	}
	
	public Future<String> checkUserState(String username ){
		Future<String> lvFuture = Future.future();
		this.queryDocument(new JsonObject().put(FieldName.USERNAME, username), handler->{
			if(handler.result()!=null) {
				if(handler.result().isEmpty()) {
					lvFuture.fail("Username not found");
				}else {
					lvFuture.complete(handler.result().get(0).getString(FieldName.STATUS));
				}
			}else {
				lvFuture.fail("Usernam not found");
			}
		});
		return lvFuture;
	}

	public void updateUserInfo(String userID, String fullName, Date birthDate, String national, String ward_city) {
		JsonObject query = new JsonObject().put("_id", userID);
		JsonObject newData = new JsonObject();
		Utils.putNotNull(newData, FieldName.FULLNAME, fullName);
		Utils.putNotNull(newData, FieldName.BIRTHDATE, birthDate);
		Utils.putNotNull(newData, FieldName.NATIONAL, national);
		Utils.putNotNull(newData, FieldName.WARD_CITY, ward_city);
		this.updateDocument(query, newData, null, handler -> {
			if (handler.succeeded()) {
				this.CompleteGenerateResponse(CodeMapping.U0000.toString(), "", null);
			} else {
				this.CompleteGenerateResponse(CodeMapping.U1111.toString(), handler.cause().getMessage(), null);
			}
		});
	}

	public void ResetPassword(String username, String newPassword) {
		mvFutureResponse = Future.future();
		JsonObject query = new JsonObject().put(FieldName.USERNAME, username);
		this.queryDocument(query, handler -> {
			if (handler.succeeded() && handler.result() != null) {
				if (handler.result().isEmpty() || handler.result().get(0) == null) {
					this.CompleteGenerateResponse(CodeMapping.U1111.toString(), CodeMapping.U1111.value(), null);
				} else {
					JsonObject userData = handler.result().get(0);
					if (userData.getString(FieldName.STATUS).equals("L")) {
						this.CompleteGenerateResponse(CodeMapping.U3333.toString(), CodeMapping.U3333.value(), null);
						return;
					}
					JsonObject newPasswordData = new JsonObject().put(FieldName.PASSWORD, newPassword);
					this.updateDocument(new JsonObject().put(FieldName._ID, userData.getString(FieldName._ID)),
							newPasswordData, null, reset -> {
								if (reset.succeeded()) {
									this.CompleteGenerateResponse(CodeMapping.C0000.toString(),
											"Reset password complete", null);
								} else {
									this.CompleteGenerateResponse(CodeMapping.U6666.toString(),
											CodeMapping.U6666.value(), null);
								}
							});
				}
			} else {
				getMvFutureResponse().fail("Username not Found");
				getMvFutureResponse().complete();
			}
		});
	}

	public Future<JsonObject> changePassword(String username, String oldPassword, String newPassword) {
		mvFutureResponse = Future.future();
		JsonObject query = new JsonObject().put(FieldName.USERNAME, username);
		this.queryDocument(query, handler -> {
			if (handler.succeeded() && handler.result() != null) {
				if (handler.result().isEmpty() || handler.result().get(0) == null) {
					this.CompleteGenerateResponse(CodeMapping.U1111.toString(), CodeMapping.U1111.value(), null);
				} else {
					JsonObject userData = handler.result().get(0);
					if (Encrypt.compare(oldPassword, userData.getString(FieldName.PASSWORD))) {
						// check old password success
						// Check new password
						String ecryptPassword = newPassword;

						try {
							ecryptPassword = Encrypt.encode(newPassword);

						} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
							e.printStackTrace();
						}

						JsonObject newPasswordData = new JsonObject().put(FieldName.PASSWORD, ecryptPassword);
						this.updateDocument(new JsonObject().put(FieldName._ID, userData.getString("_id")),
								newPasswordData, null, changepasswordresult -> {
									if (changepasswordresult.succeeded()) {
										// ChangePassword Success, Complete
										this.CompleteGenerateResponse(CodeMapping.C0000.toString(),
												"Change Password Suceess", userData);
										loginSuccess(userData.getString("_id"));
									} else {
										this.CompleteGenerateResponse(CodeMapping.U5555.toString(),
												"Change Password Failed", null);
									}
								});
					} else {
						// Check oldPassword wrong
						this.CompleteGenerateResponse(CodeMapping.U2222.toString(), CodeMapping.U2222.value(), null);
						loginFaile(userData);
					}
				}
			} else {
				getMvFutureResponse().fail("Username not Found");
				getMvFutureResponse().complete();
			}
		});
		return mvFutureResponse;

	}

	public void loginSuccess(String ID) {
		JsonObject query = new JsonObject().put("_id", ID);
		JsonObject updateData = new JsonObject().put(FieldName.FAILLOGINCOUNT, 0);
		this.updateDocument(query, updateData, new UpdateOptions(false), handler -> {
		});
	}

	public void loginFaile(JsonObject userData) {
		JsonObject query = new JsonObject().put("_id", userData.getString("_id"));
		int failCount = userData.getInteger(FieldName.FAILLOGINCOUNT)==null?0:userData.getInteger(FieldName.FAILLOGINCOUNT);

		JsonObject updateData = new JsonObject().put(FieldName.FAILLOGINCOUNT, ++failCount);
		if (failCount >= getMaxFaild()) {
			updateData.put(FieldName.STATE, "L");
		}
		this.updateDocument(query, updateData, new UpdateOptions(false), handler -> {
		});
	}
	
	public void updateAccountToPendingSate(String username, String reject) {
		this.updateDocument(new JsonObject().put(FieldName.USERNAME, username), new JsonObject().put(FieldName.STATE, "P").put(FieldName.REMARK, reject), new UpdateOptions(false), handler -> {
		});
	}

	public static int getMaxFaild() {
		return maxFaild;
	}

	public static void setMaxFaild(int maxFaild) {
		UserDao.maxFaild = maxFaild;
	}

}
