package com.coutinho.atvp.server;

import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import com.coutinho.atvp.db.DBFacade;
import com.coutinho.atvp.entities.Ranking;
import com.coutinho.atvp.exception.EntityValidationException;
import com.google.appengine.api.datastore.Entity;
import javax.persistence.EntityNotFoundException;

public class RankingServiceImpl extends BaseServlet {

	protected JSONObject doLoad(HttpServletRequest req)
			throws EntityNotFoundException {
		Long id = Long.valueOf(req.getParameter("id"));
		Ranking r = (Ranking) (DBFacade.getInstance().get(id, Ranking.class));
		JSONObject json = r.toJSON();
		return json;
	}

	protected long doPersist(HttpServletRequest req)
			throws EntityNotFoundException, EntityValidationException {
		Ranking m = null;
		if (req.getParameter("id") != null) {
			Long id = Long.valueOf(req.getParameter("id"));
			m = (Ranking) (DBFacade.getInstance().get(id, Ranking.class));
			m.fromEntity(req.getParameterMap());
		} else {
			m = new Ranking(req);
		}

		return DBFacade.getInstance().persist(m).getId();
	}

	protected JSONArray doList(HttpServletRequest req) {
		Long id = Long.valueOf(req.getParameter("idManager"));
		Iterable<Entity> rankings = DBFacade.getInstance()
				.queryRankingsFrom(id);
		JSONArray list = new JSONArray();
		for (Iterator iterator = rankings.iterator(); iterator.hasNext();) {
			Entity type = (Entity) iterator.next();
			Ranking r = new Ranking(type);
			list.put(r.toJSON());
		}
		return list;
	}

}
