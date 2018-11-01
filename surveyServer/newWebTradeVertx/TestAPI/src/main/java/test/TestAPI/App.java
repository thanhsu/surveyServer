package test.TestAPI;

import java.util.Scanner;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.ext.web.client.WebClient;

/**
 * Hello world!
 *
 */
public class App {
	public static void main(String[] args) {
		// HttpClient httpClient = Vertx.vertx().createHttpClient();
		WebClient httpClient = WebClient.create(Vertx.vertx());
		String token = "e9cfc7395adab4232f17d5837c98101f";
		String url = "data.fixer.io";
		System.out.println("++++++++++++++++Test API Money https://fixer.io++++++++++++");

		System.out.println("1. Get list CCY");
		System.out.println("2. Convert");
		System.out.println("Please Choose: ");
		Scanner sc = new Scanner(System.in);
		int i = sc.nextInt();
		boolean continute = true;

		while (continute) {

			while (i == 0) {
				System.out.println("Please Choose: ");
				i = sc.nextInt();
			}
			try {
				switch (i) {
				case 1:
					String tmp = "/api/latest?access_key=" + token + "&format=1";
					httpClient.get(url, tmp).send(res -> {
						if (res.succeeded()) {
							System.out.println("Received List Currentcy: " + res.result().bodyAsJsonObject());
						} else {
							System.out.println("Not success:" + res.cause().getMessage());
						}
					});

					continute = false;
					break;

				case 2:
					System.out.println("Exchange Money: \nFrom: ");
					sc = new Scanner(System.in);
					String from = sc.nextLine();
					System.out.println("To: ");

					String to = sc.nextLine();
					System.out.println("Amount: ");
					String amount = sc.nextLine();

					String uri = "/api/convert?access_key=" + token + "&from=" + from + "&to=" + to + "&amount="
							+ amount;
					httpClient.get(url, uri).send(res -> {
						if (res.succeeded()) {

							System.out.println("Received Convert: " + res.result().bodyAsJsonObject());

						} else {
							System.out.println("Not success");
						}
					});
					continute = false;
					break;

				default:
					continute = true;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
