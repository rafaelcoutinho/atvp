package com.coutinho.atvp.server;

import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.coutinho.atvp.db.DBFacade;
import com.coutinho.atvp.db.EntityNotFoundException;
import com.coutinho.atvp.entities.Friendship;
import com.coutinho.atvp.entities.Match;
import com.coutinho.atvp.entities.MatchState;
import com.coutinho.atvp.entities.Player;
import com.coutinho.atvp.entities.Set;
import com.coutinho.atvp.exception.EntityValidationException;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

public class PlayerServiceImpl extends BaseServlet {

	protected JSONObject doLoad(HttpServletRequest req) throws EntityNotFoundException {
		Long id = Long.valueOf(req.getParameter("id"));
		Player r = (Player) (DBFacade.getInstance().get(id, Player.class));
		JSONObject json = r.toJSON();
		return json;
	}

	class Stats {
		int totalMatches = 0;
		int totalMatchesWon = 0;
		int totalMatchesLost = 0;
		int totalSetWon = 0;
		int totalSetLost = 0;
		int totalGamesWon = 0;
		int totalGamesLost = 0;

		public JSONObject toJson() throws JSONException {
			JSONObject json = new JSONObject();
			json.put("totalMatches", totalMatches);
			json.put("totalMatchesWon", totalMatchesWon);
			json.put("totalMatchesLost", totalMatchesLost);
			json.put("totalSetWon", totalSetWon);
			json.put("totalSetLost", totalSetLost);
			json.put("totalGamesWon", totalGamesWon);
			json.put("totalGamesLost", totalGamesLost);
			return json;
		}
	}

	public String dotest(HttpServletRequest req) throws EntityNotFoundException {
		return "áéíãôpoxa";
	}

	public String dorememberpwd(HttpServletRequest req) throws EntityNotFoundException {
		String email = (req.getParameter("email"));
		Player r = (Player) (DBFacade.getInstance().getPlayerByEmail(email));
		String newPWD = "a" + ((int) Math.random() * 100) + "b" + ((int) Math.random() * 100);

		r.setPassword(SignUpServiceImpl.getMd5edPwd(newPWD));
		JSONObject json = new JSONObject();
		try {
			DBFacade.getInstance().persist(r);
			new SendEmail().sendEmail("Olá " + r.getName() + ",<br>"

			+ " você solicitou uma nova senha no ATVP. <br>Segue a nova senha gerada: '" + newPWD + "'<br>Utilize-a para se autenticar no ATVP.<br>",

			"Ranking de Tênis Virtual", r.getEmail(), r.getName());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return json.toString();
	}

	public String doloadfriends(HttpServletRequest req) throws EntityNotFoundException {
		try {
			Long id = Long.valueOf(req.getParameter("id"));
			Player r = (Player) (DBFacade.getInstance().get(id, Player.class));

			JSONArray friendsArr = new JSONArray();
			Iterable<Entity> friends = DBFacade.getInstance().getAllFriends(r.getKey());

			for (Iterator<Entity> iterator = friends.iterator(); iterator.hasNext();) {
				Entity entity = (Entity) iterator.next();
				System.err.println("key " + KeyFactory.keyToString(entity.getKey()));
				Friendship fship = new Friendship(entity);

				System.err.println("f " + fship.getId() + " " + entity);
				Player friend = null;
				if (fship.getIdPlayerOne().equals(r.getKey())) {
					friend = (Player) DBFacade.getInstance().get(fship.getIdPlayerTwo(), Player.class);
				} else {
					friend = (Player) DBFacade.getInstance().get(fship.getIdPlayerOne(), Player.class);
				}
				JSONObject friendJson = friend.toJSON();
				System.err.println("friend " + friend.getId());
				int matches = DBFacade.getInstance().getAllMatchesBetween(r.getKey(), friend.getKey());
				matches += DBFacade.getInstance().getAllMatchesBetween(friend.getKey(), r.getKey());

				friendJson.put("matches", matches);

				friendsArr.put(friendJson);
			}

			return friendsArr.toString();
		} catch (Exception e) {
			logException("Erro carregando lista de amigos de " + req.getParameter("id"), e);
			return null;
		}

	}

	public String doloadwithstats(HttpServletRequest req) throws EntityNotFoundException {
		try {
			Long id = Long.valueOf(req.getParameter("id"));
			Player r = (Player) (DBFacade.getInstance().get(id, Player.class));

			JSONObject json = r.toJSON();
			Iterable<Entity> playerMatches = DBFacade.getInstance().getAllMatchesForPlayer(r.getId());
			Stats allTimes = new Stats();
			Stats currentMonth = new Stats();
			Stats currentYear = new Stats();
			Calendar yearStart = Calendar.getInstance(req.getLocale());
			yearStart.set(Calendar.HOUR_OF_DAY, 0);
			yearStart.set(Calendar.MINUTE, 0);
			yearStart.set(Calendar.SECOND, 0);
			yearStart.set(Calendar.DAY_OF_YEAR, 0);

			System.out.println(yearStart.getTime() + " yearStart");
			System.out.println(yearStart.getTime() + " monthStart");

			Calendar monthStart = Calendar.getInstance(req.getLocale());
			monthStart.set(Calendar.DAY_OF_MONTH, 0);
			monthStart.set(Calendar.HOUR_OF_DAY, 0);
			monthStart.set(Calendar.MINUTE, 0);
			monthStart.set(Calendar.SECOND, 0);

			for (Iterator iterator = playerMatches.iterator(); iterator.hasNext();) {
				Entity entity = (Entity) iterator.next();
				Match match = new Match(entity);
				updateMatchStats(match);
				allTimes.totalMatches++;
				if (yearStart.getTimeInMillis() < match.getDate()) {
					currentYear.totalMatches++;
				}
				if (monthStart.getTimeInMillis() < (match.getDate())) {
					currentMonth.totalMatches++;
				}
				List<Entity> setEntities = DBFacade.getInstance().getAllSetsFrom(match);
				int setsWonInMatch = 0;
				for (Iterator iterator2 = setEntities.iterator(); iterator2.hasNext();) {
					Entity entity2 = (Entity) iterator2.next();
					Set set = new Set(entity2);
					if (match.getIdPlayerOne().equals(r.getId())) {
						allTimes.totalGamesWon += set.getPlayerOneGames();
						allTimes.totalGamesLost += set.getPlayerTwoGames();
						if (yearStart.getTimeInMillis() < match.getDate()) {
							currentYear.totalGamesWon += set.getPlayerOneGames();
							currentYear.totalGamesLost += set.getPlayerTwoGames();
						}
						if (monthStart.getTimeInMillis() < (match.getDate())) {
							currentMonth.totalGamesWon += set.getPlayerOneGames();
							currentMonth.totalGamesLost += set.getPlayerTwoGames();
						}

						if (set.getPlayerOneGames() > set.getPlayerTwoGames()) {
							if (yearStart.getTimeInMillis() < match.getDate()) {
								currentYear.totalSetWon++;
							}
							if (monthStart.getTimeInMillis() < (match.getDate())) {
								currentMonth.totalSetWon++;
							}
							allTimes.totalSetWon++;
							setsWonInMatch++;
						} else {
							if (yearStart.getTimeInMillis() < match.getDate()) {
								currentYear.totalSetLost++;
							}
							if (monthStart.getTimeInMillis() < (match.getDate())) {
								currentMonth.totalSetLost++;
							}
							allTimes.totalSetLost++;
							setsWonInMatch--;
						}
					} else {

						if (yearStart.getTimeInMillis() < match.getDate()) {
							currentYear.totalGamesWon += set.getPlayerTwoGames();
							currentYear.totalGamesLost += set.getPlayerOneGames();
						}
						if (monthStart.getTimeInMillis() < (match.getDate())) {
							currentMonth.totalGamesWon += set.getPlayerTwoGames();
							currentMonth.totalGamesLost += set.getPlayerOneGames();
						}
						allTimes.totalGamesWon += set.getPlayerTwoGames();
						allTimes.totalGamesLost += set.getPlayerOneGames();
						if (set.getPlayerOneGames() > set.getPlayerTwoGames()) {
							if (yearStart.getTimeInMillis() < match.getDate()) {
								currentYear.totalSetLost++;
							}
							if (monthStart.getTimeInMillis() < (match.getDate())) {
								currentMonth.totalSetLost++;
							}
							allTimes.totalSetLost++;
							setsWonInMatch--;

						} else {
							if (yearStart.getTimeInMillis() < match.getDate()) {
								currentYear.totalSetWon++;
							}
							if (monthStart.getTimeInMillis() < (match.getDate())) {
								currentMonth.totalSetWon++;
							}
							allTimes.totalSetWon++;
							setsWonInMatch++;
						}
					}
				}
				if (setsWonInMatch > 0) {
					allTimes.totalMatchesWon++;
					if (yearStart.getTimeInMillis() < match.getDate()) {
						currentYear.totalMatchesWon++;
					}
					if (monthStart.getTimeInMillis() < (match.getDate())) {
						currentMonth.totalMatchesWon++;
					}
				} else if (setsWonInMatch < 0) {
					allTimes.totalMatchesLost++;
					if (yearStart.getTimeInMillis() < match.getDate()) {
						currentYear.totalMatchesLost++;
					}
					if (monthStart.getTimeInMillis() < (match.getDate())) {
						currentMonth.totalMatchesLost++;
					}
				}

			}

			json.put("allTimes", allTimes.toJson());
			json.put("currentMonth", currentMonth.toJson());
			json.put("currentYear", currentYear.toJson());

			return json.toString();
		} catch (Exception e) {
			System.err.println("Erro " + e.getMessage());
			e.printStackTrace();
			return null;
		}

	}

	public static void updateMatchStats(Match match) throws EntityValidationException {
		long oldEnough = System.currentTimeMillis() - (1000 * 60 * 60 * 24 / 4);
		if (match.getDate() < oldEnough && MatchState.Pending.equals(match.getMatchState())) {
			List<Entity> setEntities = DBFacade.getInstance().getAllSetsFrom(match);
			int totalSetsPlayerTwo = 0;
			int totalSetsPlayerOne = 0;
			for (Iterator iterator2 = setEntities.iterator(); iterator2.hasNext();) {
				Entity entity2 = (Entity) iterator2.next();
				Set set = new Set(entity2);
				if (set.getPlayerOneGames() > set.getPlayerTwoGames()) {
					totalSetsPlayerOne++;
				} else if (set.getPlayerOneGames() < set.getPlayerTwoGames()) {
					totalSetsPlayerTwo++;
				}
			}
			match.setTotalSetsPlayerOne(totalSetsPlayerOne);
			match.setTotalSetsPlayerTwo(totalSetsPlayerTwo);
			if (totalSetsPlayerOne > totalSetsPlayerTwo) {
				match.setWinnerId(match.getIdPlayerOne());
			} else if (totalSetsPlayerOne < totalSetsPlayerTwo) {
				match.setWinnerId(match.getIdPlayerTwo());
			}
			match.setMatchState(MatchState.Completed);
			DBFacade.getInstance().persist(match);

		}
	}

	public String doupdatelicense(HttpServletRequest req) throws EntityNotFoundException, EntityValidationException {
		Player m = null;
		if (req.getParameter("ik") != null) {
			Key k = KeyFactory.stringToKey(req.getParameter("ik"));
			m = (Player) (DBFacade.getInstance().get(k, Player.class));

		} else if (req.getParameter("email") != null) {
			m = DBFacade.getInstance().getPlayerByEmail(req.getParameter("email"));
		} else {
			throw new RuntimeException("Cadastro deve serfeito via servido signup");
		}
		m.fromRequest(req);
		DBFacade.getInstance().persist(m);
		m = DBFacade.getInstance().getPlayerByEmail(req.getParameter("email"));
		return m.toJSONString();
	}

	protected long doPersist(HttpServletRequest req) throws EntityNotFoundException, EntityValidationException {
		Player m = null;
		if (req.getParameter("id") != null) {
			Long id = Long.valueOf(req.getParameter("id"));
			m = (Player) (DBFacade.getInstance().get(id, Player.class));
			m.fromEntity(req.getParameterMap());
		} else {
			throw new RuntimeException("Cadastro deve ser feito via servico signup");
		}

		return DBFacade.getInstance().persist(m).getId();
	}

	protected JSONArray doList(HttpServletRequest resp) {
		Iterable<Entity> rankings = DBFacade.getInstance().queryAll(Player.class);
		JSONArray list = new JSONArray();
		for (Iterator iterator = rankings.iterator(); iterator.hasNext();) {
			Entity type = (Entity) iterator.next();
			Player r = new Player(type);
			list.put(r.toJSON());
		}
		return list;
	}

}
