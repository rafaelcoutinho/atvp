package com.coutinho.atvp.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class MailTester extends HttpServlet {
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		SendEmail s = new SendEmail();
		String name = "a";
		s.sendEmail(
				"Olá "
						+ name
						+ ",<br>"
						+ req.getParameter("playerOneEmail")
						+ " lhe convidou para uma partida de tênis dia "
						+ " às "
						+ " pelo aplicativo ATVP, Associação de Tenistas Virtuais Pro.<br>Instale o aplicativo https://play.google.com/apps/testing/com.ionicframework.atvpmobile663442 e instale-o.<br>",
				"Ranking de Tênis Virtual", req.getParameter("to"),
				req.getParameter("name"));

	}

}
