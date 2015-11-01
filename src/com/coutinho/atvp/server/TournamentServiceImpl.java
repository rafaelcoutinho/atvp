package com.coutinho.atvp.server;

import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import com.coutinho.atvp.db.DBFacade;
import com.coutinho.atvp.entities.Ranking;
import com.coutinho.atvp.entities.Tournament;
import com.coutinho.atvp.exception.EntityValidationException;
import com.google.appengine.api.datastore.Entity;

import javax.persistence.EntityNotFoundException;

public class TournamentServiceImpl extends BaseRestServlet {

	protected JSONObject doLoad(HttpServletRequest req)
			throws EntityNotFoundException {
		Long id = Long.valueOf(req.getParameter("id"));
		Tournament r = (Tournament) (DBFacade.getInstance().get(id, Tournament.class));
		JSONObject json = r.toJSON();
		return json;
	}

	protected long doPersist(HttpServletRequest req)
			throws EntityNotFoundException, EntityValidationException {
		Tournament m = null;
		if (req.getParameter("id") != null) {
			Long id = Long.valueOf(req.getParameter("id"));
			m = (Tournament) (DBFacade.getInstance().get(id, Tournament.class));
			m.fromEntity(req.getParameterMap());
		} else {
			m = new Tournament(req);
		}

		return DBFacade.getInstance().persist(m).getId();
	}
	

	protected JSONArray doList(HttpServletRequest req) {
		Long id = Long.valueOf(req.getParameter("idManager"));
		Iterable<Entity> tournaments = DBFacade.getInstance()
				.queryManagersTournments(id);
		JSONArray list = new JSONArray();
		for (Iterator iterator = tournaments.iterator(); iterator.hasNext();) {
			Entity type = (Entity) iterator.next();
			Tournament r = new Tournament(type);
			list.put(r.toJSON());
		}
		return list;
	}

}
