package com.coutinho.atvp.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import com.coutinho.atvp.db.EntityNotFoundException;
import com.coutinho.atvp.exception.EntityValidationException;

public abstract class BaseRestServlet extends HttpServlet {
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		System.out.println(req.getPathInfo());
		System.out.println(req.getPathTranslated());
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		try {
			doPersist(req);
		} catch (EntityNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (EntityValidationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	protected abstract long doPersist(HttpServletRequest req)
			throws EntityNotFoundException, EntityValidationException;

	protected abstract JSONObject doLoad(HttpServletRequest req)
			throws EntityNotFoundException;

	protected abstract JSONArray doList(HttpServletRequest req);
}
