package com.coutinho.atvp.entities;

import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONObject;

import com.coutinho.atvp.exception.EntityValidationException;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;

public class Invitation extends DBObject<Invitation> {

	Key from;
	String to;
	protected Long since = System.currentTimeMillis();

	public Invitation(HttpServletRequest req) {
		super(req);

	}

	public Invitation(Entity entity) {
		super(entity);
	}

	public Invitation(Key pkey, String invited) {
		this.from = pkey;
		this.to = invited;
	}

	@Override
	public void validate(DatastoreService datastore) throws EntityValidationException {
		if (id == null) {
			Filter p1First = new FilterPredicate("from", FilterOperator.EQUAL, from);
			Filter p2Second = new FilterPredicate("to", FilterOperator.EQUAL, to);
			Filter p1Firstp2Second = CompositeFilterOperator.and(p1First, p2Second);

			Query q = new Query(getKind()).setFilter(p1Firstp2Second);
			PreparedQuery pq = datastore.prepare(q);
			if (pq.asSingleEntity() != null && (getId() == null || pq.asSingleEntity().getKey().getId() != getId())) {
				throw new EntityValidationException("Convite ja existente");
			}
		}

	}

	public Key getFrom() {
		return from;
	}

	public void setFrom(Key from) {
		this.from = from;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public Long getSince() {
		return since;
	}

	public void setSince(Long since) {
		this.since = since;
	}

	public static Invitation getFromJson(JSONObject invitationJson) {
		Entity ent = new Entity(KeyFactory.createKey(Invitation.class.getSimpleName(), invitationJson.getLong("id")));

		for (Iterator iterator = invitationJson.keys(); iterator.hasNext();) {
			String key = (String) iterator.next();
			if (key.equals("from")) {
				String from = invitationJson.getString("from");
				if (from != null && !from.isEmpty()) {
					from = from.substring("Player(".length());
					from = from.substring(0, from.length() - 1);
					ent.setProperty(key, KeyFactory.createKey(Player.class.getSimpleName(), Long.valueOf(from)));
				}

				continue;
			}
			ent.setProperty(key, invitationJson.get(key));

		}
		return new Invitation(ent);
	}
}
