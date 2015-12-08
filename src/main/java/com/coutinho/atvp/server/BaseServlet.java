package com.coutinho.atvp.server;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.coutinho.atvp.db.EntityNotFoundException;
import com.coutinho.atvp.exception.EntityValidationException;

public abstract class BaseServlet extends HttpServlet {
	static Logger LOG = Logger.getLogger("Web");

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		String action = req.getParameter("action");
		if ("list".equals(action)) {
			resp.getWriter().print(doList(req).toString());
		} else if ("get".equals(action)) {
			try {
				JSONObject json = doLoad(req);
				resp.getWriter().print(json);
			} catch (Exception e) {
				logException("Erro no metodo " + action, e);
				resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
			}
		} else if (action != null) {
			try {
				Method m = this.getClass().getMethod("do" + action, HttpServletRequest.class);
				String r = (String) m.invoke(this, req);
				if (r == null) {
					throw new NullPointerException("Resposta do metodo " + action + " nula!");
				}
				resp.getWriter().print(r);
			} catch (NoSuchMethodException e) {
				logException("Nao encontrou o metodo " + action, e);
				resp.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED);
			} catch (SecurityException e) {
				logException("Falha seguranca ", e);
				resp.sendError(HttpServletResponse.SC_NOT_FOUND);
			} catch (Exception e) {
				logException("Falha generica ", e);
				resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			}
		} else {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
		}

	}

	@Override
	protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String action = req.getParameter("action");

		if ("persist".equals(action)) {
			try {
				JSONObject respJson = new JSONObject();
				try {
					long id = doPersist(req);
					respJson.put("id", id);

				} catch (EntityNotFoundException e) {
					e.printStackTrace();
					resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
					respJson.put("error", "entity_not_found");

				} catch (Exception e) {
					e.printStackTrace();
					resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
					respJson.put("error", "unknown_error");
					respJson.put("msg", e.getMessage());

				}
				resp.getWriter().print(respJson.toString());
			} catch (JSONException e) {
				e.printStackTrace();
			}
		} else if (action != null) {
			try {
				Method m = this.getClass().getMethod("do" + action, HttpServletRequest.class);
				String r = (String) m.invoke(this, req);
				resp.getWriter().print(r);
			} catch (NoSuchMethodException e) {
				LOG.log(Level.WARNING, "NoSuchMethodException", e);
				resp.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED);
			} catch (SecurityException e) {
				LOG.log(Level.WARNING, "SecurityException", e);
				resp.sendError(HttpServletResponse.SC_NOT_FOUND);
			} catch (Exception e) {

				LOG.log(Level.SEVERE, "Exception", e);
				resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				resp.getWriter().print("{'error':'unknown_error','msg':'" + e.getMessage() + "'}");
			}
		} else {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
		}
	}

	protected abstract long doPersist(HttpServletRequest req) throws EntityNotFoundException, EntityValidationException;

	protected abstract JSONObject doLoad(HttpServletRequest req) throws EntityNotFoundException;

	protected abstract JSONArray doList(HttpServletRequest req);

	public static void logException(String w, Exception e) {
		LOG.log(Level.SEVERE, w, e);

	}

}
