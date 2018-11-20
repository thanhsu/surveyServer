package com.survey.dbservices;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Hashtable;

import com.survey.dbservice.dao.BaseDaoConnection;
import com.survey.dbservices.action.BaseAdminServiceAction;
import com.survey.utils.Log;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;

public class DBServiceInit extends AbstractVerticle {
	public static Hashtable<String, BaseAdminServiceAction> mvActionAuthMapping = new Hashtable<>();

	@Override
	public void init(Vertx vertx, Context context) {
		// TODO Auto-generated method stub
		super.init(vertx, context);
		Log.setMvConfig(config());
		Log.setSysTrace();
		Log.print("*************************************");
		Log.print("*******Init Db Connection***********");

		BaseDaoConnection.setDbConfig(config().getJsonObject("DBConnectionConfig"));
		BaseDaoConnection.setVertx(vertx);
		try {
			BaseDaoConnection.getInstance();
		} catch (Exception e) {
			System.out.println("Init DB Connection Failed. Systen Down");
			e.printStackTrace();
			vertx.close();
		}
		JsonArray actionMapping = config().getJsonArray("ActionMapping");
		if (actionMapping != null) {
			actionMapping.forEach(action -> {
				try {
					String actionmap = action.toString();
					Class<?> lvActionClass = Class.forName(actionmap.split("-")[1]);
					Constructor<?> lvConstructor = lvActionClass.getConstructor();
					BaseAdminServiceAction lvAuthAction = (BaseAdminServiceAction) lvConstructor.newInstance();
					mvActionAuthMapping.put(actionmap.split("-")[0], lvAuthAction);
				} catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException
						| IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					e.printStackTrace();
				}

			});
		}
	}

	@Override
	public void start(Future<Void> startFuture) throws Exception {
		// TODO Auto-generated method stub
		super.start(startFuture);
		vertx.deployVerticle(DBService.class.getName(), new DeploymentOptions().setConfig(config()).setWorker(true), res -> {
			if (res.failed()) {
				System.out.println("Service DB Init Error: " + res.cause().getMessage());
				vertx.close();
			}
		});
	}

	public static Hashtable<String, BaseAdminServiceAction> getMvActionAuthMapping() {
		return mvActionAuthMapping;
	}

	public static void setMvActionAuthMapping(Hashtable<String, BaseAdminServiceAction> mvActionAuthMapping) {
		DBServiceInit.mvActionAuthMapping = mvActionAuthMapping;
	}
}
