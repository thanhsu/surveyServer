package com.survey.utils;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

public class SurveyMailCenter {
	private static SurveyMailCenter instance;

	private JsonObject mvconfig;
	Properties props = new Properties();

	Session session;

	public static SurveyMailCenter getInstance() {
		synchronized (SurveyMailCenter.class) {
			if (instance == null) {
				instance = new SurveyMailCenter();
			}
			return instance;
		}
	}

	public void init(JsonObject config, Vertx vertx) {
		mvconfig = config;

		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", "smtp.gmail.com");
		props.put("mail.smtp.port", "587");
		session = Session.getInstance(props, new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(mvconfig.getString("username"), mvconfig.getString("password"));
			}
		});

	}

	public boolean sendEmail(String to, String subject, String cc, String messageData) {
		try {
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(mvconfig.getString("username")));
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
			message.setSubject(subject);
			message.setText(messageData);

			Transport.send(message);
			return true;
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;

	}

}
