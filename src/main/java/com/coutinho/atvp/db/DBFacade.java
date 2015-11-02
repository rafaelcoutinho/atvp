package com.coutinho.atvp.db;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.coutinho.atvp.entities.DBObject;
import com.coutinho.atvp.entities.Friendship;
import com.coutinho.atvp.entities.Game;
import com.coutinho.atvp.entities.Invitation;
import com.coutinho.atvp.entities.Match;
import com.coutinho.atvp.entities.MatchState;
import com.coutinho.atvp.entities.Player;
import com.coutinho.atvp.entities.Ranking;
import com.coutinho.atvp.entities.ScheduledMatch;
import com.coutinho.atvp.entities.Set;
import com.coutinho.atvp.entities.Tournament;
import com.coutinho.atvp.exception.EntityValidationException;
import com.coutinho.atvp.exception.LoginFailedException;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.SortDirection;

public class DBFacade {
	DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
	static DBFacade instance;

	public static DBFacade getInstance() {
		if (instance == null) {
			instance = new DBFacade();
		}
		return instance;
	}

	private DBFacade() {

	}

	public void persist(Entity entity) {
		datastore.put(entity);
	}

	public Key persist(DBObject p) throws EntityValidationException {
		Entity player = new Entity(p.getKind());
		if (p.getKey() != null) {
			player = new Entity(p.getKey());
		} else if (p.getId() != null) {
			player = new Entity(p.getKind(), p.getId());
		}

		p.setProperties(player);
		p.validate(datastore);
		Key k = datastore.put(player);
		p.setKey(k);
		return k;
	}

	public Key persist(Set p) throws EntityValidationException {
		Entity player = new Entity(p.getKind(), p.getMatch().getKey());
		if (p.getKey() != null) {
			player = new Entity(p.getKind(), p.getKey().getId(), p.getMatch()
					.getKey());
		}

		p.setProperties(player);
		p.validate(datastore);
		Key k = datastore.put(player);

		return k;
	}

	public DBObject get(long id, Class classType)
			throws EntityNotFoundException {
		Key k = KeyFactory.createKey(classType.getSimpleName(), id);
		return get(k, classType);

	}

	public Tournament getTournament(long id) throws EntityNotFoundException {
		Key k = KeyFactory.createKey(Tournament.class.getSimpleName(), id);
		Tournament sm = (Tournament) (get(k, Tournament.class));
		return sm;
	}

	public Match getMatch(long id) throws EntityNotFoundException {
		Key k = KeyFactory.createKey(Match.class.getSimpleName(), id);
		Match sm = (Match) (get(k, Match.class));
		return sm;
	}

	public ScheduledMatch getScheduledMatch(long id, boolean fetchall)
			throws EntityNotFoundException {
		Key k = KeyFactory.createKey(ScheduledMatch.class.getSimpleName(), id);
		ScheduledMatch sm = (ScheduledMatch) (get(k, ScheduledMatch.class));
		if (fetchall) {
			sm.setTournment(getTournament(sm.getId()));
			if (sm.getMatchId() != null) {
				sm.setMatch(getMatch(sm.getMatchId()));
			}
			if (sm.getNextScheduledMatchId() != null) {
				sm.setNextScheduledMatch(getScheduledMatch(
						sm.getNextScheduledMatchId(), true));
			}
		}
		return sm;

	}

	public List<ScheduledMatch> getScheduledMatchesOfTournment(long id,
			boolean fetchall) throws EntityNotFoundException {
		Filter p1Filter = new FilterPredicate("tournmentId",
				FilterOperator.EQUAL, id);

		Query q = new Query(ScheduledMatch.class.getSimpleName());
		q.addSort("round", SortDirection.ASCENDING);
		q.setFilter(p1Filter);
		PreparedQuery pq = datastore.prepare(q);
		List<ScheduledMatch> ts = new ArrayList<ScheduledMatch>();
		for (Iterator<Entity> iterator = pq.asIterable().iterator(); iterator
				.hasNext();) {
			Entity type = (Entity) iterator.next();
			ScheduledMatch t = new ScheduledMatch(type);
			ts.add(t);
		}

		return ts;

	}

	public DBObject get(Key k, Class classType) throws EntityNotFoundException {
		try {
			Entity employee = datastore.get(k);
			return (DBObject) classType.getConstructor(Entity.class)
					.newInstance(employee);
		} catch (Exception e) {

			throw new EntityNotFoundException();
		}
	}

	public Iterable<Entity> queryRankingsFrom(Long id) {
		Query q = new Query(Ranking.class.getSimpleName());
		Filter f = new FilterPredicate("idManager", FilterOperator.EQUAL, id);
		q.setFilter(f);
		PreparedQuery pq = datastore.prepare(q);
		return pq.asIterable();
	}

	public List<Tournament> queryManagersTournments(Long id) {
		Query q = new Query(Ranking.class.getSimpleName());
		Filter f = new FilterPredicate("idManager", FilterOperator.EQUAL, id);
		q.setFilter(f);
		PreparedQuery pq = datastore.prepare(q);
		List<Tournament> ts = new ArrayList<Tournament>();
		for (Iterator<Entity> iterator = pq.asIterable().iterator(); iterator
				.hasNext();) {
			Entity type = (Entity) iterator.next();
			Tournament t = new Tournament(type);
			ts.add(t);
		}
		return ts;

	}

	public Iterable<Entity> queryAll(Class class1) {
		Query q = new Query(class1.getSimpleName());
		PreparedQuery pq = datastore.prepare(q);
		return pq.asIterable();
	}

	public Iterable<Entity> queryMatchesFrom(Long id) {
		Filter p1Filter = new FilterPredicate("idRanking",
				FilterOperator.EQUAL, id);

		Query q = new Query(Match.class.getSimpleName());
		q.setFilter(p1Filter);

		return datastore.prepare(q).asIterable();

	}

	public Iterable<Entity> getAllMatchesForPlayer(Long idPlayer) {

		Filter p0NotRanked = new FilterPredicate("idRanking",
				FilterOperator.EQUAL, null);

		Filter p1Filter = new FilterPredicate("idPlayerOne",
				FilterOperator.EQUAL, idPlayer);
		Filter p2Filter = new FilterPredicate("idPlayerTwo",
				FilterOperator.EQUAL, idPlayer);
		// Use CompositeFilter to combine multiple filters
		Filter loginFilter = CompositeFilterOperator.or(p1Filter, p2Filter);
		Filter myFilter = CompositeFilterOperator.and(loginFilter, p0NotRanked);
		Query q = new Query(Match.class.getSimpleName());
		q.addSort("date", SortDirection.DESCENDING);
		q.setFilter(myFilter);

		return datastore.prepare(q).asIterable();
	}

	public List<Entity> getAllSetsFrom(Match match) {
		Query q = new Query(Set.class.getSimpleName()).setAncestor(match
				.getKey());
		q.addSort("number");

		return datastore.prepare(q).asList(FetchOptions.Builder.withDefaults());
	}

	public List<Entity> getAllGamesFrom(Set set) {
		Query q = new Query(Game.class.getSimpleName()).setAncestor(set
				.getKey());
		q.addSort("number");

		return datastore.prepare(q).asList(FetchOptions.Builder.withDefaults());
	}

	public void delete(Set set) {
		datastore.delete(set.getKey());
	}

	public void delete(Key matchKey) {
		datastore.delete(matchKey);

	}

	public Player getPlayerByLogin(String email, String md5edPwd)
			throws LoginFailedException {
		Query q = new Query(Player.class.getSimpleName());
		Filter emailFilter = new FilterPredicate("email", FilterOperator.EQUAL,
				email);
		Filter passwordFilter = new FilterPredicate("password",
				FilterOperator.EQUAL, md5edPwd);
		// Use CompositeFilter to combine multiple filters
		Filter loginFilter = CompositeFilterOperator.and(emailFilter,
				passwordFilter);

		q.setFilter(loginFilter);
		PreparedQuery pq = datastore.prepare(q);
		if (pq.countEntities(FetchOptions.Builder.withDefaults()) == 0) {
			throw new LoginFailedException();
		}
		return new Player(pq.asSingleEntity());
	}

	public Player getPlayerByEmail(String email) throws EntityNotFoundException {
		Query q = new Query(Player.class.getSimpleName());
		Filter emailFilter = new FilterPredicate("email", FilterOperator.EQUAL,
				email);
		q.setFilter(emailFilter);
		PreparedQuery pq = datastore.prepare(q);
		if (pq.countEntities(FetchOptions.Builder.withDefaults()) == 0) {
			System.err.println("nao encontrou jodador com email " + email);
			throw new EntityNotFoundException();
		}
		return new Player(pq.asSingleEntity());
	}

	public Entity getFriendShip(Key p1, Key p2) {
		Filter p1First = new FilterPredicate("idPlayerOne",
				FilterOperator.EQUAL, p1);
		Filter p1Second = new FilterPredicate("idPlayerTwo",
				FilterOperator.EQUAL, p2);

		Filter myFilter = CompositeFilterOperator.and(p1First, p1Second);

		Query q = new Query(Friendship.class.getSimpleName())
				.setFilter(myFilter);
		q.addSort("since");
		PreparedQuery pq = datastore.prepare(q);
		System.err
				.println(pq.countEntities(FetchOptions.Builder.withDefaults()));
		if (pq.countEntities(FetchOptions.Builder.withDefaults()) > 0) {
			return pq.asSingleEntity();
		} else {
			return null;
		}
	}

	public Iterable<Entity> getAllFriends(Key id) {

		Filter p1First = new FilterPredicate("idPlayerOne",
				FilterOperator.EQUAL, id);
		Filter p1Second = new FilterPredicate("idPlayerTwo",
				FilterOperator.EQUAL, id);

		Filter myFilter = CompositeFilterOperator.or(p1First, p1Second);

		Query q = new Query(Friendship.class.getSimpleName())
				.setFilter(myFilter);
		q.addSort("since");
		PreparedQuery pq = datastore.prepare(q);

		return pq.asIterable();
	}

	public Integer getAllMatchesBetween(Key p1, Key p2) {
		Filter p0NotRanked = new FilterPredicate("idRanking",
				FilterOperator.EQUAL, null);

		Filter p1Filter = new FilterPredicate("idPlayerOne",
				FilterOperator.EQUAL, p1.getId());
		Filter p2Filter = new FilterPredicate("idPlayerTwo",
				FilterOperator.EQUAL, p2.getId());
		// Use CompositeFilter to combine multiple filters
		Filter matchesBetween = CompositeFilterOperator.and(p1Filter, p2Filter);

		Filter myFilter = CompositeFilterOperator.and(matchesBetween,
				p0NotRanked);
		Query q = new Query(Match.class.getSimpleName());
		// q.addSort("date", SortDirection.DESCENDING);
		q.setFilter(myFilter);

		return datastore.prepare(q).countEntities(
				FetchOptions.Builder.withDefaults());
	}

	/**
	 * List how invited me
	 * 
	 * @param email
	 * @return
	 */
	public Iterable<Entity> listInvitationsByEmail(String email) {
		Filter myFilter = new FilterPredicate("to", FilterOperator.EQUAL, email);

		Query q = new Query(Invitation.class.getSimpleName());
		q.addSort("since", SortDirection.DESCENDING);
		q.setFilter(myFilter);

		return datastore.prepare(q).asIterable();
	}

	public Iterable<Entity> listInvitationsByInviter(Key playerKey) {
		Filter myFilter = new FilterPredicate("from", FilterOperator.EQUAL,
				playerKey);

		Query q = new Query(Invitation.class.getSimpleName());
		q.addSort("since", SortDirection.DESCENDING);
		q.setFilter(myFilter);

		return datastore.prepare(q).asIterable();
	}

	public Iterable<Entity> getAllPendingMatches() {
		Filter myFilter = new FilterPredicate("state", FilterOperator.EQUAL,
				MatchState.Pending.name());

		Query q = new Query(Match.class.getSimpleName());
		q.setFilter(myFilter);

		return datastore.prepare(q).asIterable();
	}

}
