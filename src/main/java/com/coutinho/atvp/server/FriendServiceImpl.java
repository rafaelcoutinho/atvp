package com.coutinho.atvp.server;

import java.util.Iterator;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.coutinho.atvp.db.DBFacade;
import com.coutinho.atvp.db.EntityNotFoundException;
import com.coutinho.atvp.entities.Friendship;
import com.coutinho.atvp.entities.Invitation;
import com.coutinho.atvp.entities.Player;
import com.coutinho.atvp.exception.EntityValidationException;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

public class FriendServiceImpl extends BaseServlet {

	protected JSONObject doLoad(HttpServletRequest req)
			throws EntityNotFoundException {
		Long id = Long.valueOf(req.getParameter("id"));
		Friendship r = (Friendship) (DBFacade.getInstance().get(id,
				Friendship.class));
		JSONObject json = r.toJSON();
		return json;
	}

	public String dosavefriendship(HttpServletRequest req)
			throws EntityNotFoundException {
		try {
			Long id1 = Long.valueOf(req.getParameter("idPlayerOne"));
			Key p1key = KeyFactory.createKey(Player.class.getSimpleName(), id1);
			Long id2 = Long.valueOf(req.getParameter("idPlayerTwo"));
			Key p2key = KeyFactory.createKey(Player.class.getSimpleName(), id2);
			Friendship fship = new Friendship(p1key, p2key);
			DBFacade.getInstance().persist(fship);
			return "ok";
		} catch (Exception e) {
			e.printStackTrace();
			return "error";
		}
	}

	public String doacceptinvitation(HttpServletRequest req)
			throws EntityNotFoundException {
		boolean accepted = true;
		JSONObject resp = handleAcceptDeclineInvitation(req, accepted);
		return resp.toString();

	}

	public String dodeclineinvitation(HttpServletRequest req)
			throws EntityNotFoundException {
		boolean accepted = false;
		JSONObject resp = handleAcceptDeclineInvitation(req, accepted);
		return resp.toString();

	}

	private JSONObject handleAcceptDeclineInvitation(HttpServletRequest req,
			boolean accepted) {
		JSONObject resp = new JSONObject();
		try {
			Long id1 = Long.valueOf(req.getParameter("id"));
			Long idPlayer = Long.valueOf(req.getParameter("idPlayer"));
			Key inviteKey = KeyFactory.createKey(
					Invitation.class.getSimpleName(), id1);
			Key playerKey = KeyFactory.createKey(Player.class.getSimpleName(),
					idPlayer);
			Invitation invitation = (Invitation) DBFacade.getInstance().get(
					inviteKey, Invitation.class);
			if (accepted) {
				Friendship fship = new Friendship(invitation.getFrom(),
						playerKey);
				DBFacade.getInstance().persist(fship);
				resp.put("success", "friendship_ok");
			} else {
				resp.put("success", "invitation_removed");
			}
			DBFacade.getInstance().delete(inviteKey);

		} catch (Exception e) {
			e.printStackTrace();
			try {
				resp.put("error", "failed");
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		return resp;
	}

	public String doloadinvitations(HttpServletRequest req)
			throws EntityNotFoundException {
		try {
			String invitedEmail = req.getParameter("email");
			Iterable<Entity> list = DBFacade.getInstance()
					.listInvitationsByEmail(invitedEmail);
			Logger LOG = Logger.getLogger("TESTE");
			LOG.warning("inviter email " + invitedEmail);
			JSONArray invitations = new JSONArray();
			for (Entity entity : list) {
				Invitation inv = new Invitation(entity);
				JSONObject invite = inv.toJSON();

				invite.put("inviter",
						DBFacade.getInstance().get(inv.getFrom(), Player.class)
								.toJSON());
				invitations.put(invite);
			}

			return invitations.toString();
		} catch (Exception e) {

			e.printStackTrace();
			JSONObject resp = new JSONObject();
			System.err.println(e.getMessage());
			for (int i = 0; i < e.getStackTrace().length; i++) {
				System.err.println(e.getStackTrace()[i].getClassName() + ":"
						+ e.getStackTrace()[i].getLineNumber());
			}
			try {
				resp.put("error", "failed");
				resp.put("error_details", e.getMessage());
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return resp.toString();
		}
	}

	public String doloadinvitationssent(HttpServletRequest req)
			throws EntityNotFoundException {
		try {
			Long idPlayer = Long.valueOf(req.getParameter("id"));

			Key playerKey = KeyFactory.createKey(Player.class.getSimpleName(),
					idPlayer);

			Iterable<Entity> list = DBFacade.getInstance()
					.listInvitationsByInviter(playerKey);
			Logger LOG = Logger.getLogger("TESTE");
			JSONArray invitationsSent = new JSONArray();
			for (Entity entity : list) {
				Invitation inv = new Invitation(entity);
				JSONObject invite = inv.toJSON();
				invitationsSent.put(invite);
			}

			return invitationsSent.toString();
		} catch (Exception e) {

			e.printStackTrace();
			JSONObject resp = new JSONObject();
			System.err.println(e.getMessage());
			for (int i = 0; i < e.getStackTrace().length; i++) {
				System.err.println(e.getStackTrace()[i].getClassName() + ":"
						+ e.getStackTrace()[i].getLineNumber());
			}
			try {
				resp.put("error", "failed");
				resp.put("error_details", e.getMessage());
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return resp.toString();
		}
	}

	public String doinvite(HttpServletRequest req)
			throws EntityNotFoundException {
		JSONObject resp = new JSONObject();
		String invited = req.getParameter("invited");
		Key pkey = null;
		try {
			if (req.getParameter("id") != null) {
				Long id = Long.valueOf(req.getParameter("id"));
				pkey = KeyFactory.createKey(Player.class.getSimpleName(), id);
			} else {
				resp.put("error", "missing_inviter");
				return resp.toString();
			}
			if (invited != null) {
				invited = req.getParameter("invited").toLowerCase();
				if (DBFacade.getInstance().getPlayerByEmail(invited) != null) {
					Player inviter = (Player) DBFacade.getInstance().get(pkey,
							Player.class);
					Player invitedPLayer = (Player) DBFacade.getInstance()
							.getPlayerByEmail(invited);
					System.err.println(invitedPLayer);
					System.err.println(invitedPLayer.getEmail());
					System.err.println(invitedPLayer.getKey());
					System.err.println("--");
					System.err.println(inviter);
					System.err.println(inviter.getEmail());
					System.err.println(inviter.getKey());

					if (DBFacade.getInstance().getFriendShip(inviter.getKey(),
							invitedPLayer.getKey()) != null
							|| DBFacade.getInstance().getFriendShip(
									invitedPLayer.getKey(), inviter.getKey()) != null) {
						resp.put("error", "already_friends");
					} else {

						try {

							Invitation invitation = new Invitation(pkey,
									invited);

							Key p2Key = DBFacade.getInstance().persist(
									invitation);

							new SendEmail()
									.sendEmail(
											"Olá "
													+ invitedPLayer.getName()

													+ ",<br>"
													+ inviter.getName()
													+ " quer ser seu rival no ATVP, Associação de Tenistas Virtuais Pro. Entre no aplicativo e aceite ou recuse o convite de rivalidade.<br>",
											"Ranking de Tênis Virtual",
											invited, invited);
							resp.put("email", "email_sent");

						} catch (Exception e1) {

							e1.printStackTrace();
							resp.put("email", "email_failed");
							resp.put("email_error", e1.getMessage());
						}
						resp.put("success", "invited");
					}
					return resp.toString();
				} else {
					System.err.println("no entity found sending new email");
					throw new EntityNotFoundException();
				}

			} else {
				resp.put("error", "missing_email");
				return resp.toString();
			}
		} catch (EntityNotFoundException e) {
			try {
				Invitation invitation = new Invitation(pkey, invited);

				Key p2Key = DBFacade.getInstance().persist(invitation);

				Player p = (Player) DBFacade.getInstance().get(pkey,
						Player.class);

				new SendEmail()
						.sendEmail(
								"Olá,"

										+ "<br>"
										+ p.getName()
										+ " lhe convidou para participar do aplicativo ATVP, Associação de Tenistas Virtuais Pro. <br><a href=\"https://play.google.com/store/apps/details?id=com.ionicframework.atvpmobile663442\"> <img alt=\"Get it on Google Play\" src=\"https://developer.android.com/images/brand/pt-br_generic_rgb_wo_45.png\" /></a>.<br>",
								"Ranking de Tênis Virtual", invited, invited);
				resp.put("success", "email_sent");
				return resp.toString();
			} catch (Exception e1) {
				e1.printStackTrace();
				try {
					resp.put("error", "email_failed");
					resp.put("email_error", e1.getMessage());
					resp.put("isnewinvited", true);
				} catch (JSONException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}
				return resp.toString();
			}

		} catch (Exception e) {
			e.printStackTrace();
			System.err.println(e.getMessage());
			for (int i = 0; i < e.getStackTrace().length; i++) {
				System.err.println(e.getStackTrace()[i].getClassName() + ":"
						+ e.getStackTrace()[i].getLineNumber());
			}
			try {
				resp.put("error", e.getMessage());
				resp.put("email_error", e.getMessage());
			} catch (JSONException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			return resp.toString();
		}

	}

	public String doloadfriends(HttpServletRequest req)
			throws EntityNotFoundException {
		try {
			Long id = Long.valueOf(req.getParameter("id"));
			Player r = (Player) (DBFacade.getInstance().get(id, Player.class));

			JSONObject json = r.toJSON();
			JSONArray friendsArr = new JSONArray();
			Iterable<Entity> friends = DBFacade.getInstance().getAllFriends(
					r.getKey());

			for (Iterator iterator = friends.iterator(); iterator.hasNext();) {
				Entity entity = (Entity) iterator.next();
				Friendship fship = new Friendship(entity);
				Player friend = null;
				if (fship.getIdPlayerOne().equals(r.getKey())) {
					friend = (Player) DBFacade.getInstance().get(
							fship.getIdPlayerTwo(), Player.class);
				} else {
					friend = (Player) DBFacade.getInstance().get(
							fship.getIdPlayerOne(), Player.class);
				}
				JSONObject friendJson = friend.toJSON();

				int matches = DBFacade.getInstance().getAllMatchesBetween(
						r.getKey(), friend.getKey());
				matches += DBFacade.getInstance().getAllMatchesBetween(
						friend.getKey(), r.getKey());

				friendJson.put("matches", matches);

				friendsArr.put(friendJson);
			}

			json.put("friends", friendsArr);

			return json.toString();
		} catch (Exception e) {
			System.err.println("Erro " + e.getMessage());
			e.printStackTrace();
			return null;
		}

	}

	protected long doPersist(HttpServletRequest req)
			throws EntityNotFoundException, EntityValidationException {
		Friendship m = null;
		if (req.getParameter("id") != null) {
			Long id = Long.valueOf(req.getParameter("id"));
			m = (Friendship) (DBFacade.getInstance().get(id, Friendship.class));
			m.fromEntity(req.getParameterMap());
		} else {
			m = new Friendship(req);
			m.fromEntity(req.getParameterMap());
		}

		return DBFacade.getInstance().persist(m).getId();
	}

	protected JSONArray doList(HttpServletRequest resp) {

		JSONArray list = new JSONArray();

		return list;
	}

}
