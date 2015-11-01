package com.coutinho.atvp.entities;

import javax.servlet.http.HttpServletRequest;

import com.coutinho.atvp.exception.EntityValidationException;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;

public class Friendship extends DBObject<Friendship> {

	protected Key idPlayerOne;
	protected Key idPlayerTwo;
	protected Long since = System.currentTimeMillis();

	public Friendship(HttpServletRequest req) {
		super(req);

	}

	public Friendship(Entity entity) {
		super(entity);
	}

	public Friendship(Key from, Key key) {
		idPlayerOne = from;
		idPlayerTwo = key;
	}

	@Override
	public void validate(DatastoreService datastore)
			throws EntityValidationException {
		if (id == null) {
			Filter p1First = new FilterPredicate("idPlayerOne",
					FilterOperator.EQUAL, idPlayerOne);
			Filter p2Second = new FilterPredicate("idPlayerTwo",
					FilterOperator.EQUAL, idPlayerTwo);
			Filter p1Firstp2Second = CompositeFilterOperator.and(p1First,
					p2Second);

			Filter p2First = new FilterPredicate("idPlayerOne",
					FilterOperator.EQUAL, idPlayerTwo);
			Filter p1Second = new FilterPredicate("idPlayerTwo",
					FilterOperator.EQUAL, idPlayerOne);
			Filter p2Firstp1Second = CompositeFilterOperator.and(p2First,
					p1Second);

			Filter myFilter = CompositeFilterOperator.or(p1Firstp2Second,
					p2Firstp1Second);

			Query q = new Query(getKind()).setFilter(myFilter);
			PreparedQuery pq = datastore.prepare(q);
			if (pq.asSingleEntity() != null
					&& (getId() == null || pq.asSingleEntity().getKey().getId() != getId())) {
				throw new EntityValidationException("Amizade já existente");
			}
		}

	}

	public Key getIdPlayerOne() {
		return idPlayerOne;
	}

	public void setIdPlayerOne(Key idPlayerOne) {
		this.idPlayerOne = idPlayerOne;
	}

	public Key getIdPlayerTwo() {
		return idPlayerTwo;
	}

	public void setIdPlayerTwo(Key idPlayerTwo) {
		this.idPlayerTwo = idPlayerTwo;
	}

	public Long getSince() {
		return since;
	}

	public void setSince(Long since) {
		this.since = since;
	}

}
