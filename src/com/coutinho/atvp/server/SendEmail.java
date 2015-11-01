package com.coutinho.atvp.server;

import java.io.UnsupportedEncodingException;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class SendEmail {
	public void sendEmailPlain(String msgBody, String subject, String toemail,
			String toname) throws UnsupportedEncodingException {
		Properties props = new Properties();
		Session session = Session.getDefaultInstance(props, null);

		try {

			Message msg = new MimeMessage(session);
			msg.setFrom(new InternetAddress(
					"atvp@tenis-virtual-players.appspotmail.com",
					"ATVP Ranking de Tenis Virtual"));
			msg.addRecipient(Message.RecipientType.TO, new InternetAddress(
					toemail, toname));
			msg.setSubject(subject);
			msg.setText(msgBody);
			Transport.send(msg);
		} catch (AddressException e) {
			e.printStackTrace();
		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}

	public void sendEmail(String msgBody, String subject, String toemail,
			String toname) throws UnsupportedEncodingException {
		Properties props = new Properties();
		Session session = Session.getDefaultInstance(props, null);

		try {
			String encodingOptions = "text/html; charset=UTF-8";
			MimeMessage msg = new MimeMessage(session);
			msg.setHeader("Content-Type", encodingOptions);
			msg.setFrom(new InternetAddress(
					"atvp@tenis-virtual-players.appspotmail.com",
					"Ranking de Tênis Virtual", "UTF-8"));

			Multipart mp = new MimeMultipart();

			msg.setContent(msgBody, "text/html; charset=utf-8");
			msg.addRecipient(Message.RecipientType.TO, new InternetAddress(
					toemail, toname, "UTF-8"));

			msg.setSubject(subject, "UTF-8");

			Transport.send(msg);

		} catch (Exception e) {
			System.err.println("Nao foi");
			e.printStackTrace();
		}
	}

}
