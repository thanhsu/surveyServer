package com.survey.dbservice.dao;

import com.survey.utils.Log;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;

public class BaseDaoConnection {
	private static JsonObject dbConfig = new JsonObject();
	private static Vertx vertx = null;

	public static Vertx getVertx() {
		return vertx;
	}

	public static void setVertx(Vertx vertx) {
		BaseDaoConnection.vertx = vertx;
	}

	public static JsonObject getDbConfig() {
		return dbConfig;
	}

	public static void setDbConfig(JsonObject dbConfig) {
		BaseDaoConnection.dbConfig = dbConfig;
	}

	private io.vertx.ext.mongo.MongoClient mongoClient = null;

	// Singleton
	private static BaseDaoConnection instDbManager = null;

	public io.vertx.ext.mongo.MongoClient getMongoClient() {
		return mongoClient;
	}

	public void setMongoClient(io.vertx.ext.mongo.MongoClient mongoClient) {
		this.mongoClient = mongoClient;
	}

	/**
	 * Lock Constructor
	 */
	private BaseDaoConnection() {
	}

	/**
	 * 
	 * @return instDbManager
	 */
	public static synchronized BaseDaoConnection getInstance() {
		if (instDbManager == null) {
			instDbManager = new BaseDaoConnection();
			if(instDbManager.getMongoClient()==null) {
				instDbManager.init();
			}
		}
		return instDbManager;
	}

	public void init() {
		connectDB();
	}

	/**
	 * 
	 * @return mongoClient
	 */
	private MongoClient connectDB() {
		if (mongoClient == null) {
			try {
				mongoClient = MongoClient.createShared(getVertx(), getDbConfig());
			} catch (Exception e) {
				Log.print("Can not connect to DB: " + e);
			}
		}
		return mongoClient;
	}

}
