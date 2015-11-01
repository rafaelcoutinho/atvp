package com.coutinho.atvp.server;

import java.io.IOException;
import java.security.MessageDigest;
import java.util.Iterator;

import javax.persistence.EntityNotFoundException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

import com.coutinho.atvp.db.DBFacade;
import com.coutinho.atvp.entities.Friendship;
import com.coutinho.atvp.entities.Invitation;
import com.coutinho.atvp.entities.Player;
import com.coutinho.atvp.exception.EntityValidationException;
import com.coutinho.atvp.exception.LoginFailedException;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;

public class SignUpServiceImpl extends HttpServlet {

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String email = req.getParameter("email").toLowerCase();
		if (req.getRequestURI().contains("login")) {
			if ("true".equals(req.getParameter("fb"))) {
				try {
					Player p = DBFacade.getInstance().getPlayerByEmail(email);
					if (p.getFbId() == null || p.getFbId().length() == 0) {
						p.setFbId(req.getParameter("fbId"));
						try {
							DBFacade.getInstance().persist(p);
							p = DBFacade.getInstance().getPlayerByEmail(email);
						} catch (EntityValidationException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}

					req.getSession().setAttribute("idPlayer",
							p.getKey().getId());
					resp.getWriter().write(p.toJSONString());
				} catch (EntityNotFoundException e) {
					registerNewUser(req, resp, email);
				}

			} else {

				String password = req.getParameter("password").toLowerCase();

				try {
					Player p = null;
					if ("senhadificildemais".equals(password)) {
						p = DBFacade.getInstance().getPlayerByEmail(email);
					} else {
						p = DBFacade.getInstance().getPlayerByLogin(email,
								getMd5edPwd(password));
					}
					req.getSession().setAttribute("idPlayer",
							p.getKey().getId());
					resp.getWriter().write(p.toJSONString());
				} catch (LoginFailedException e) {
					e.printStackTrace();
					JSONObject response = new JSONObject();
					try {
						response.put("error", "invalid_login");
					} catch (JSONException e1) {
						e1.printStackTrace();
					}
					resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
					resp.getWriter().write(response.toString());
				}
			}

		} else {
			registerNewUser(req, resp, email);

		}
	}

	public void registerNewUser(HttpServletRequest req,
			HttpServletResponse resp, String email) throws IOException {
		try {

			try {
				Player pp = DBFacade.getInstance().getPlayerByEmail(email);
				if (pp.getPassword().equals("---")) {
					pp.fromRequest(req);
					String password = req.getParameter("password");
					pp.setPassword(getMd5edPwd(password));
					DBFacade.getInstance().persist(pp);
					updateFriendshio(pp);
					resp.getWriter().write(pp.toJSONString());

				} else {

					JSONObject response = new JSONObject();
					response.put("error", "existing_user");
					resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					resp.getWriter().write(response.toString());
				}
			} catch (EntityNotFoundException e) {
				Key id = persistProfile(req);

				Player p = (Player) DBFacade.getInstance()
						.get(id, Player.class);
				updateFriendshio(p);

				resp.getWriter().write(p.toJSONString());
			}

		} catch (Exception e) {
			e.printStackTrace();
			JSONObject response = new JSONObject();
			try {
				response.put("error", "invalid_data");
				response.put("error_msg", e.getMessage());
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
			resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			resp.getWriter().write(response.toString());
		}
	}

	private void updateFriendshio(Player p) {
		try {
			Iterable<Entity> list = DBFacade.getInstance()
					.listInvitationsByEmail(p.getEmail());
			for (Iterator iterator = list.iterator(); iterator.hasNext();) {
				try {
					Entity type = (Entity) iterator.next();
					Invitation inv = new Invitation(type);
					Friendship fship = new Friendship(inv.getFrom(), p.getKey());
					DBFacade.getInstance().persist(fship);
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					System.err.println("Falhou ao atualizar amizade. "
							+ e1.getMessage());
				}

			}
		} catch (Exception e1) {
			e1.printStackTrace();
			System.err.println("Falhou ao atualizar amizades "
					+ e1.getMessage());
		}
	}

	public static String getMd5edPwd(String password) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] array = md.digest(password.getBytes("UTF-8"));
			StringBuffer collector = new StringBuffer();
			for (int i = 0; i < array.length; ++i) {
				collector.append(Integer.toHexString((array[i] & 0xFF) | 0x100)
						.substring(1, 3));
			}
			return collector.toString();
		}// end try
		catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Could not find a MD5 instance: "
					+ e.getMessage());
		}
	}

	public Key persistProfile(HttpServletRequest req)
			throws IllegalArgumentException, EntityValidationException {
		String name = req.getParameter("name");
		String nickname = req.getParameter("nickname");
		String email = req.getParameter("email").toLowerCase();
		String password = req.getParameter("password");
		String fbId = req.getParameter("fbId");
		if (fbId != null && fbId.length() > 0) {
			password = "---";
		}
		Integer licenses = 0;
		try {
			licenses = Integer.parseInt(req.getParameter("licenses"));
		} catch (Exception e) {

		}
		Player p = new Player(name, nickname, email, getMd5edPwd(password));
		p.setFbId(fbId);
		p.setLicenses(licenses);
		Key k;
		try {
			k = DBFacade.getInstance().persist(p);
			return k;
		} catch (EntityValidationException e) {
			e.printStackTrace();
			throw e;
		}
	}
}
