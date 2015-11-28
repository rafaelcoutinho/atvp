package com.coutinho.atvp.server;

import java.util.Iterator;
import java.util.List;

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
	protected long doPersist(HttpServletRequest req)
			throws EntityNotFoundException, EntityValidationException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected JSONObject doLoad(HttpServletRequest req)
			throws EntityNotFoundException {
		if ("ppp".equals(req.getParameter("pwd"))) {
			Iterable<Entity> rankings = DBFacade.getInstance().queryAll(
					Player.class);
			JSONArray playerList = new JSONArray();
			for (Iterator iterator = rankings.iterator(); iterator.hasNext();) {
				Entity type = (Entity) iterator.next();
				Player r = new Player(type);
				JSONObject pJson = r.toJSON();
				JSONArray matchesJarr = new JSONArray();
				Match m = null;
				try {
					Iterable<Entity> matches = DBFacade.getInstance()
							.getAllMatchesForPlayer(r.getId());
					for (Iterator iterator2 = matches.iterator(); iterator2
							.hasNext();) {
						Entity mEntity = (Entity) iterator2.next();
						m = new Match(mEntity);
						List<Entity> sets = DBFacade.getInstance()
								.getAllSetsFrom(m);
						JSONArray setsArr = new JSONArray();
						for (Iterator iterator3 = sets.iterator(); iterator3
								.hasNext();) {
							Entity entity = (Entity) iterator3.next();
							Set s = new Set(entity);
							setsArr.put(s.toJSON());
						}
						JSONObject mjson = m.toJSON();
						mjson.put("sets", setsArr);
						matchesJarr.put(mjson);
					}
				} catch (Exception e) {
					e.printStackTrace();
					if (m != null) {
						System.err.println("Falhou carregando sets da partida "
								+ m.getKey().getId());
					}
					System.err.println(e.getMessage());
					for (int i = 0; i < e.getStackTrace().length; i++) {
						System.err.println(e.getStackTrace()[i].getClassName()
								+ ":" + e.getStackTrace()[i].getLineNumber());
					}
				}
				pJson.put("matches", matchesJarr);
				JSONArray invitationsArr = new JSONArray();
				try {
					Iterable<Entity> invs = DBFacade.getInstance()
							.listInvitationsByInviter(r.getKey());
					for (Iterator iterator2 = invs.iterator(); iterator2
							.hasNext();) {
						Entity invitation = (Entity) iterator2.next();
						Invitation inv = new Invitation(invitation);
						invitationsArr.put(inv.toJSON());
					}
				} catch (Exception e) {
					e.printStackTrace();
					System.err.println(e.getMessage());
					for (int i = 0; i < e.getStackTrace().length; i++) {
						System.err.println(e.getStackTrace()[i].getClassName()
								+ ":" + e.getStackTrace()[i].getLineNumber());
					}
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
