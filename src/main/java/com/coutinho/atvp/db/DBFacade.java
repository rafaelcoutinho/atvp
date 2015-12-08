package com.coutinho.atvp.db;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

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
import com.coutinho.atvp.entities.TournmentParticipant;
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

	private int listScheduledMatch(Long tournmentId, Long idMatch, Long id) {

		Filter tourFilter = new FilterPredicate("tournmentId", FilterOperator.EQUAL, tournmentId);

		Filter matchFilter = new FilterPredicate("matchId", FilterOperator.EQUAL, idMatch);
		Filter myFilter1 = CompositeFilterOperator.and(tourFilter, matchFilter);
		if (id != null) {
			Filter roundFilter = new FilterPredicate("id", FilterOperator.NOT_EQUAL, id);
			myFilter1 = CompositeFilterOperator.and(tourFilter, roundFilter, matchFilter);
		}

		Query q = new Query(ScheduledMatch.class.getSimpleName());
		q.setFilter(myFilter1);

		return datastore.prepare(q).countEntities(FetchOptions.Builder.withDefaults());
	}

	public Key persist(ScheduledMatch p) throws EntityValidationException {
		if (listScheduledMatch(p.getTournmentId(), p.getMatchId(), p.getId()) > 0) {
			throw new IllegalArgumentException("Já existe uma partida agenda para este tornoeio com o mesmo match");
		}

		Entity player = new Entity(p.getKind());
		// check if there is any other

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

	public Key persist(DBObject p) throws EntityValidationException {
		Entity dbobject = new Entity(p.getKind());
		if (p.getKey() != null) {
			dbobject = new Entity(p.getKey());
		} else if (p.getId() != null) {
			dbobject = new Entity(p.getKind(), p.getId());
		}

		p.setProperties(dbobject);
		p.validate(datastore);
		Key k = datastore.put(dbobject);
		p.setKey(k);
		return k;
	}

	public Key persist(Set p) throws EntityValidationException {
		Entity set = new Entity(p.getKind(), p.getMatch().getKey());
		if (p.getKey() != null) {
			set = new Entity(p.getKind(), p.getKey().getId(), p.getMatch().getKey());
		}

		p.setProperties(set);
		p.validate(datastore);
		Key k = datastore.put(set);

		return k;
	}

	public DBObject get(long id, Class classType) throws EntityNotFoundException {
		Key k = KeyFactory.createKey(classType.getSimpleName(), id);
		return get(k, classType);

	}

	public Tournament getTournament(long id) throws EntityNotFoundException {
		Key k = KeyFactory.createKey(Tournament.class.getSimpleName(), id);
		Tournament sm = (Tournament) (get(k, Tournament.class));
		return sm;
	}

	public Match getMatch(long id, boolean fetch) throws EntityNotFoundException {
		Key k = KeyFactory.createKey(Match.class.getSimpleName(), id);
		Match sm = (Match) (get(k, Match.class));
		if (fetch) {
			if (sm.getIdPlayerOne() != null) {
				Key kp = KeyFactory.createKey(Player.class.getSimpleName(), sm.getIdPlayerOne());
				sm.setPlayerOne((Player) get(kp, Player.class));
			}
			if (sm.getIdPlayerTwo() != null) {
				Key kp = KeyFactory.createKey(Player.class.getSimpleName(), sm.getIdPlayerTwo());
				sm.setPlayerTwo((Player) get(kp, Player.class));
			}
		}
		return sm;
	}

	public ScheduledMatch getScheduledMatch(long id, boolean fetchall) throws EntityNotFoundException {
		Key k = KeyFactory.createKey(ScheduledMatch.class.getSimpleName(), id);
		ScheduledMatch sm = (ScheduledMatch) (get(k, ScheduledMatch.class));
		if (fetchall) {
			sm.setTournment(getTournament(sm.getTournmentId()));
			if (sm.getMatchId() != null) {
				sm.setMatch(getMatch(sm.getMatchId(), true));
			}
			if (sm.getNextScheduledMatchId() != null) {
				sm.setNextScheduledMatch(getScheduledMatch(sm.getNextScheduledMatchId(), true));
			}
		}
		return sm;

	}

	public TournmentParticipant getTournmentParticipants(Long id, Long idParticipant, boolean b) throws EntityNotFoundException {
		Filter p1Filter = new FilterPredicate("tournmentId", FilterOperator.EQUAL, id);
		Filter p2Filter = new FilterPredicate("participantId", FilterOperator.EQUAL, idParticipant);

		Filter bothFilter = CompositeFilterOperator.and(p1Filter, p2Filter);

		Query q = new Query(TournmentParticipant.class.getSimpleName());
		q.setFilter(bothFilter);
		PreparedQuery pq = datastore.prepare(q);
		if (pq.asIterator().hasNext()) {

			Entity type = (Entity) pq.asIterable().iterator().next();
			TournmentParticipant t = new TournmentParticipant(type);
			if (b) {
				t.setParticipant((Player) get(t.getParticipantId(), Player.class));
			}
			return t;
		}
		return null;

	}

	Logger LOG = Logger.getLogger("DB");

	public List<TournmentParticipant> getTournmentParticipants(Long id, boolean b) throws EntityNotFoundException {
		Filter p1Filter = new FilterPredicate("tournmentId", FilterOperator.EQUAL, id);
		Query q = new Query(TournmentParticipant.class.getSimpleName());
		q.addSort("date", SortDirection.ASCENDING);
		q.setFilter(p1Filter);
		PreparedQuery pq = datastore.prepare(q);
		List<TournmentParticipant> ts = new ArrayList<TournmentParticipant>();
		for (Iterator<Entity> iterator = pq.asIterable().iterator(); iterator.hasNext();) {
			Entity type = (Entity) iterator.next();
			LOG.log(Level.INFO, "t " + type);
			TournmentParticipant t = new TournmentParticipant(type);
			System.out.println("t " + t.getParticipantId());
			if (b) {
				Player p = (Player) get(t.getParticipantId(), Player.class);
				t.setParticipant(p);
			}
			ts.add(t);
		}
		LOG.log(Level.INFO, "passou " + ts.size());
		return ts;
	}

	public List<ScheduledMatch> getScheduledMatchesOfTournment(long id, boolean fetchall) throws EntityNotFoundException {
		Filter p1Filter = new FilterPredicate("tournmentId", FilterOperator.EQUAL, id);

		Query q = new Query(ScheduledMatch.class.getSimpleName());
		q.addSort("round", SortDirection.ASCENDING);
		q.setFilter(p1Filter);
		PreparedQuery pq = datastore.prepare(q);
		List<ScheduledMatch> ts = new ArrayList<ScheduledMatch>();
		for (Iterator<Entity> iterator = pq.asIterable().iterator(); iterator.hasNext();) {
			Entity type = (Entity) iterator.next();
			ScheduledMatch t = new ScheduledMatch(type);
			if (fetchall) {
				if (t.getMatchId() != null) {
					t.setMatch(getMatch(t.getMatchId(), true));
				}
			}
			ts.add(t);
		}

		return ts;

	}

	public DBObject get(Key k, Class classType) throws EntityNotFoundException {
		try {
			Entity employee = datastore.get(k);
			return (DBObject) classType.getConstructor(Entity.class).newInstance(employee);
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
		for (Iterator<Entity> iterator = pq.asIterable().iterator(); iterator.hasNext();) {
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
		Filter p1Filter = new FilterPredicate("idRanking", FilterOperator.EQUAL, id);

		Query q = new Query(Match.class.getSimpleName());
		q.setFilter(p1Filter);

		return datastore.prepare(q).asIterable();

	}

	public Iterable<Entity> getAllMatchesForPlayer(Long idPlayer) {

		return getAllMatchesForPlayer(idPlayer, null, null);

	}

	public Iterable<Entity> getAllMatchesForPlayer(Long idPlayer, Long start, Long enddate) {

		Filter p1Filter = new FilterPredicate("idPlayerOne", FilterOperator.EQUAL, idPlayer);
		Filter p2Filter = new FilterPredicate("idPlayerTwo", FilterOperator.EQUAL, idPlayer);

		// Use CompositeFilter to combine multiple filters
		Filter loginFilter = CompositeFilterOperator.or(p1Filter, p2Filter);
		Filter finalFilter = loginFilter;
		if (start != null) {
			Filter pdateStart = new FilterPredicate("date", FilterOperator.GREATER_THAN_OR_EQUAL, start);
			finalFilter = CompositeFilterOperator.and(loginFilter, pdateStart);
			if (enddate != null) {
				Filter pdateEnd = new FilterPredicate("date", FilterOperator.LESS_THAN, enddate);
				finalFilter = CompositeFilterOperator.and(finalFilter, pdateEnd);

			}
		}

		//
		Query q = new Query(Match.class.getSimpleName());

		q.addSort("date", SortDirection.DESCENDING);
		q.setFilter(finalFilter);

		PreparedQuery pq = datastore.prepare(q);
		return pq.asIterable(FetchOptions.Builder.withDefaults());
	}

	public List<Entity> getAllSetsFrom(Match match) {
		Query q = new Query(Set.class.getSimpleName()).setAncestor(match.getKey());
		q.addSort("number");

		return datastore.prepare(q).asList(FetchOptions.Builder.withDefaults());
	}

	public List<Entity> getAllGamesFrom(Set set) {
		Query q = new Query(Game.class.getSimpleName()).setAncestor(set.getKey());
		q.addSort("number");

		return datastore.prepare(q).asList(FetchOptions.Builder.withDefaults());
	}

	public void delete(Set set) {
		datastore.delete(set.getKey());
	}

	public void delete(Key matchKey) {
		datastore.delete(matchKey);

	}

	public Player getPlayerByLogin(String email, String md5edPwd) throws LoginFailedException {
		Query q = new Query(Player.class.getSimpleName());
		Filter emailFilter = new FilterPredicate("email", FilterOperator.EQUAL, email);
		Filter passwordFilter = new FilterPredicate("password", FilterOperator.EQUAL, md5edPwd);
		// Use CompositeFilter to combine multiple filters
		Filter loginFilter = CompositeFilterOperator.and(emailFilter, passwordFilter);

		q.setFilter(loginFilter);
		PreparedQuery pq = datastore.prepare(q);
		if (pq.countEntities(FetchOptions.Builder.withDefaults()) == 0) {
			throw new LoginFailedException();
		}
		return new Player(pq.asSingleEntity());
	}

	public Player getPlayerByEmail(String email) throws EntityNotFoundException {
		Query q = new Query(Player.class.getSimpleName());
		Filter emailFilter = new FilterPredicate("email", FilterOperator.EQUAL, email);
		q.setFilter(emailFilter);
		PreparedQuery pq = datastore.prepare(q);
		if (pq.countEntities(FetchOptions.Builder.withDefaults()) == 0) {
			LOG.warning("nao encontrou jodador com email " + email);
			throw new EntityNotFoundException();
		}
		return new Player(pq.asSingleEntity());
	}

	public Entity getFriendShip(Key p1, Key p2) {
		Filter p1First = new FilterPredicate("idPlayerOne", FilterOperator.EQUAL, p1);
		Filter p1Second = new FilterPredicate("idPlayerTwo", FilterOperator.EQUAL, p2);

		Filter myFilter = CompositeFilterOperator.and(p1First, p1Second);

		Query q = new Query(Friendship.class.getSimpleName()).setFilter(myFilter);
		q.addSort("since");
		PreparedQuery pq = datastore.prepare(q);

		if (pq.countEntities(FetchOptions.Builder.withDefaults()) > 0) {
			return pq.asSingleEntity();
		} else {
			return null;
		}
	}

	public Iterable<Entity> getAllFriends(Key id) {

		Filter p1First = new FilterPredicate("idPlayerOne", FilterOperator.EQUAL, id);
		Filter p1Second = new FilterPredicate("idPlayerTwo", FilterOperator.EQUAL, id);

		Filter myFilter = CompositeFilterOperator.or(p1First, p1Second);

		Query q = new Query(Friendship.class.getSimpleName()).setFilter(myFilter);
		q.addSort("since");
		PreparedQuery pq = datastore.prepare(q);

		return pq.asIterable();
	}

	public List<Entity> listRoundMatches(Long tournmentId, Integer round) {

		Filter tourFilter = new FilterPredicate("tournmentId", FilterOperator.EQUAL, tournmentId);
		Filter roundFilter = new FilterPredicate("round", FilterOperator.EQUAL, round);
		Filter matchFilter = new FilterPredicate("matchId", FilterOperator.NOT_EQUAL, null);
		Filter myFilter1 = CompositeFilterOperator.and(tourFilter, roundFilter, matchFilter);
		Query q = new Query(ScheduledMatch.class.getSimpleName());
		q.setFilter(myFilter1);
		List<Long> matchIds = new ArrayList<Long>();
		Iterable<Entity> sms = datastore.prepare(q).asIterable();
		for (Iterator iterator = sms.iterator(); iterator.hasNext();) {
			ScheduledMatch sm = new ScheduledMatch((Entity) iterator.next());
			matchIds.add(sm.getMatchId());
		}
		if (matchIds.isEmpty()) {
			return new ArrayList<Entity>();
		}
		Filter matchesFilter = new FilterPredicate("key.id", FilterOperator.IN, matchIds);
		Query matchQuery = new Query(Match.class.getSimpleName());
		matchQuery.setFilter(matchesFilter);

		return datastore.prepare(matchQuery).asList(FetchOptions.Builder.withDefaults());
	}

	public Iterable<Entity> getAllMatchesBetween(Key p1, Key p2) {

		Filter p1Filter = new FilterPredicate("idPlayerOne", FilterOperator.EQUAL, p1.getId());
		Filter p2Filter = new FilterPredicate("idPlayerTwo", FilterOperator.EQUAL, p2.getId());

		Filter p12Filter = new FilterPredicate("idPlayerOne", FilterOperator.EQUAL, p2.getId());
		Filter p21Filter = new FilterPredicate("idPlayerTwo", FilterOperator.EQUAL, p1.getId());

		Filter p1Part = CompositeFilterOperator.and(p12Filter, p21Filter);
		// Use CompositeFilter to combine multiple filters
		Filter p2part = CompositeFilterOperator.and(p1Filter, p2Filter);
		Filter matchesBetween = CompositeFilterOperator.or(p1Part, p2part);

		Query q = new Query(Match.class.getSimpleName());
		// q.addSort("date", SortDirection.DESCENDING);
		q.setFilter(matchesBetween);

		return datastore.prepare(q).asIterable();
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
		Filter myFilter = new FilterPredicate("from", FilterOperator.EQUAL, playerKey);

		Query q = new Query(Invitation.class.getSimpleName());
		q.addSort("since", SortDirection.DESCENDING);
		q.setFilter(myFilter);

		return datastore.prepare(q).asIterable();
	}

	public Iterable<Entity> getAllPendingMatches() {
		Filter myFilter = new FilterPredicate("state", FilterOperator.EQUAL, MatchState.Pending.name());

		Query q = new Query(Match.class.getSimpleName());
		q.setFilter(myFilter);

		return datastore.prepare(q).asIterable();
	}

}
