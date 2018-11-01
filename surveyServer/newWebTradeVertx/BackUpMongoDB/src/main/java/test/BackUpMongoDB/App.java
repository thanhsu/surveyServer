package test.BackUpMongoDB;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * Hello world!
 *
 */
public class App {
	public static void main(String[] args) {
		System.out.println("Hello World!");
		File lvTmp = new File("cache/bietduochoatchat.txt");
		try {
			BaseDaoConnection.setDbConfig(new JsonObject());
			BaseDaoConnection.setVertx(Vertx.vertx());
			final FileOutputStream input = new FileOutputStream(lvTmp);
			BaseDaoConnection.getInstance().getMongoClient().find("bietduochoatchats", new JsonObject(), handler -> {
				if (handler.succeeded()) {
					try {
						handler.result().forEach(action -> {
							String id = action.getString("id");
							String hamluong = action.getString("hamluong");
							String hoatchat = action.getString("hoatchat");
							String duoclieu = action.getString("duoclieu");
							String hoatchat_name = action.getString("hoatchat_name");
							String sql = "INSERT INTO BIETDUOCHOATCHAT";
							try {
								input.write(sql.getBytes());
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

						});
						input.close();
					} catch (Exception e) {

						e.printStackTrace();
					}

				} else {

				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		} finally {

		}
	}
}
