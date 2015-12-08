package com.coutinho.atvp.server;

import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import com.coutinho.atvp.db.DBFacade;
import com.coutinho.atvp.db.EntityNotFoundException;
import com.coutinho.atvp.entities.Invitation;
import com.coutinho.atvp.entities.Match;
import com.coutinho.atvp.entities.Player;
import com.coutinho.atvp.entities.Set;
import com.coutinho.atvp.exception.EntityValidationException;
import com.google.appengine.api.datastore.Entity;

public class Backuper extends BaseServlet {

	@Override
	protected long doPersist(HttpServletRequest req) throws EntityNotFoundException, EntityValidationException {
		if ("pdp".equals(req.getParameter("code"))) {
			String jsonStr = req.getParameter("jsonbackup");
			JSONObject json = new JSONObject(jsonStr);
			JSONArray players = json.getJSONArray("players");
			for (int i = 0; i < players.length(); i++) {
				JSONObject player = players.getJSONObject(i);

				Player p = Player.fromJson(player);
				LOG.fine(p.toJSONString());
				DBFacade.getInstance().persist(p);
				LOG.fine(p.toJSONString());
			}

			for (int i = 0; i < players.length(); i++) {
				JSONObject player = players.getJSONObject(i);
				JSONArray invitations = player.getJSONArray("invitations");
				for (int j = 0; j < invitations.length(); j++) {
					JSONObject invitationJson = invitations.getJSONObject(j);
					Invitation invitation = Invitation.getFromJson(invitationJson);
					DBFacade.getInstance().persist(invitation);
				}
				JSONArray matches = player.getJSONArray("matches");
				for (int j = 0; j < matches.length(); j++) {
					JSONObject matchJson = matches.getJSONObject(j);
					Match m = Match.fromJson(matchJson);
					DBFacade.getInstance().persist(m);
					JSONArray setsJson = matchJson.getJSONArray("sets");
					for (int k = 0; k < setsJson.length(); k++) {
						JSONObject set = setsJson.getJSONObject(k);
						LOG.fine(set.toString());
						Set s = Set.getFromJson(set, m);
						DBFacade.getInstance().persist(s);
					}
				}

			}

			return 1;
		}
		return 0;
	}

	@Override
	protected JSONObject doLoad(HttpServletRequest req) throws EntityNotFoundException {
		if ("ppp".equals(req.getParameter("pwd"))) {
			Iterable<Entity> rankings = DBFacade.getInstance().queryAll(Player.class);
			JSONArray playerList = new JSONArray();
			for (Iterator iterator = rankings.iterator(); iterator.hasNext();) {
				Entity type = (Entity) iterator.next();
				if (type.hasProperty("ik")) {
					type.removeProperty("ik");
					DBFacade.getInstance().persist(type);
				}
				if (type.hasProperty("key")) {
					type.removeProperty("key");
					DBFacade.getInstance().persist(type);
				}
				Player r = new Player(type);
				JSONObject pJson = r.toJSON();
				pJson.put("password", r.getPassword());
				JSONArray matchesJarr = new JSONArray();
				Match m = null;
				try {
					Iterable<Entity> matches = DBFacade.getInstance().getAllMatchesForPlayer(r.getId());
					int counter = 0;
					for (Iterator iterator2 = matches.iterator(); iterator2.hasNext();) {
						counter++;
						Entity mEntity = (Entity) iterator2.next();
						if (mEntity.hasProperty("ik")) {
							mEntity.removeProperty("ik");
							DBFacade.getInstance().persist(mEntity);
						}
						if (mEntity.hasProperty("key")) {
							mEntity.removeProperty("key");
							DBFacade.getInstance().persist(mEntity);
						}
						m = new Match(mEntity);
						List<Entity> sets = DBFacade.getInstance().getAllSetsFrom(m);
						JSONArray setsArr = new JSONArray();
						for (Iterator iterator3 = sets.iterator(); iterator3.hasNext();) {
							Entity entity = (Entity) iterator3.next();
							if (entity.hasProperty("ik")) {
								entity.removeProperty("ik");
								DBFacade.getInstance().persist(entity);
							}
							if (entity.hasProperty("key")) {
								entity.removeProperty("key");
								DBFacade.getInstance().persist(entity);
							}
							Set s = new Set(entity);
							setsArr.put(s.toJSON());
						}
						JSONObject mjson = m.toJSON();
						mjson.put("sets", setsArr);
						matchesJarr.put(mjson);
					}
					LOG.warning("Matches for " + r.getEmail() + " = " + counter);
				} catch (Exception e) {
					e.printStackTrace();
					if (m != null) {
						LOG.warning("Falhou carregando sets da partida " + m.getKey().getId());
					}
					LOG.log(Level.SEVERE, "Falhou carregando sets da partida", e);
				}
				pJson.put("matches", matchesJarr);
				JSONArray invitationsArr = new JSONArray();
				try {
					Iterable<Entity> invs = DBFacade.getInstance().listInvitationsByInviter(r.getKey());
					for (Iterator iterator2 = invs.iterator(); iterator2.hasNext();) {
						Entity invitation = (Entity) iterator2.next();
						Invitation inv = new Invitation(invitation);
						invitationsArr.put(inv.toJSON());
					}
				} catch (Exception e) {
					LOG.log(Level.SEVERE, "Falhou carregando invitationsArr", e);
				}
				pJson.put("invitations", invitationsArr);
				playerList.put(pJson);
			}
			JSONObject backup = new JSONObject();
			backup.put("date", System.currentTimeMillis());
			backup.put("players", playerList);
			return backup;
		} else {
			throw new IllegalArgumentException("Invalid pwd");
		}

	}

	@Override
	protected JSONArray doList(HttpServletRequest req) {
		// TODO Auto-generated method stub
		return null;
	}

}
