package com.coutinho.atvp.server;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.logging.Logger;

import javax.persistence.EntityNotFoundException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.coutinho.atvp.exception.EntityValidationException;

public abstract class BaseServlet extends HttpServlet {
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		String action = req.getParameter("action");
		if ("list".equals(action)) {

			resp.getWriter().print(doList(req).toString());
		} else if ("get".equals(action)) {
			try {
				JSONObject json = doLoad(req);
				resp.getWriter().print(json);
			} catch (Exception e) {
				resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
						e.getMessage());
			}
		} else if (action != null) {
			try {
				Method m = this.getClass().getMethod("do" + action,
						HttpServletRequest.class);
				String r = (String) m.invoke(this, req);
				resp.getWriter().print(r);
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
				resp.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED);
			} catch (SecurityException e) {
				e.printStackTrace();
				resp.sendError(HttpServletResponse.SC_NOT_FOUND);
			} catch (Exception e) {
				Logger LOG = Logger.getLogger("TESTE");
				LOG.warning("FALHOU");
				e.printStackTrace();
				System.err.println(e.getMessage());
				for (int i = 0; i < e.getStackTrace().length; i++) {
					System.err.println(e.getStackTrace()[i].getClassName()
							+ ":" + e.getStackTrace()[i].getLineNumber());
				}
				resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			}
		} else {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
		}

	}

	@Override
	protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		resp.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
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
				Method m = this.getClass().getMethod("do" + action,
						HttpServletRequest.class);
				String r = (String) m.invoke(this, req);
				resp.getWriter().print(r);
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
				resp.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED);
			} catch (SecurityException e) {
				e.printStackTrace();
				resp.sendError(HttpServletResponse.SC_NOT_FOUND);
			} catch (Exception e) {
				e.printStackTrace();
				System.err.println(e.getMessage());
				for (int i = 0; i < e.getStackTrace().length; i++) {
					System.err.println(e.getStackTrace()[i].getClassName()
							+ ":" + e.getStackTrace()[i].getLineNumber());
				}
				resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				resp.getWriter().print(
						"{'error':'unknown_error','msg':'" + e.getMessage()
								+ "'}");
			}
		} else {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
		}
	}

	protected abstract long doPersist(HttpServletRequest req)
			throws EntityNotFoundException, EntityValidationException;

	protected abstract JSONObject doLoad(HttpServletRequest req)
			throws EntityNotFoundException;

	protected abstract JSONArray doList(HttpServletRequest req);

}
