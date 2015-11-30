package com.coutinho.atvp.server;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.coutinho.atvp.db.DBFacade;
import com.coutinho.atvp.db.EntityNotFoundException;
import com.coutinho.atvp.entities.Friendship;
import com.coutinho.atvp.entities.Game;
import com.coutinho.atvp.entities.Invitation;
import com.coutinho.atvp.entities.Match;
import com.coutinho.atvp.entities.MatchState;
import com.coutinho.atvp.entities.Player;
import com.coutinho.atvp.entities.Ranking;
import com.coutinho.atvp.entities.Set;
import com.coutinho.atvp.exception.EntityValidationException;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

public class MatchServiceImpl extends BaseServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1797644948413460182L;
	static int a = 1;

	protected JSONObject doLoad(HttpServletRequest req) throws EntityNotFoundException {
		String idRankingStr = req.getParameter("idRanking");
		Long idRanking = null;
		if (idRankingStr != null && idRankingStr.length() > 0) {
			idRanking = Long.valueOf(req.getParameter("idRanking"));
		}

		Long id = Long.valueOf(req.getParameter("id"));
		Key matchKey = KeyFactory.createKey(Match.class.getSimpleName(), id);
		Match match = (Match) DBFacade.getInstance().get(matchKey, Match.class);

		try {

			PlayerServiceImpl.updateMatchStats(match);

			match = (Match) DBFacade.getInstance().get(matchKey, Match.class);

		} catch (EntityValidationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		JSONObject json = match.toJSON();
		return json;
	}

	@Override
	protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		JSONObject respJson = new JSONObject();
		try {
			Key matchKey = null;
			if (req.getParameter("key") != null) {
				matchKey = KeyFactory.stringToKey(req.getParameter("key"));

				DBFacade.getInstance().delete(matchKey);
				respJson.put("success", true);
			}
		} catch (Exception e) {

			e.printStackTrace();
			try {
				respJson.put("error", "failed_to_delete");
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
		resp.getWriter().print(respJson.toString());
	}

	public String doupdatematches(HttpServletRequest req) throws EntityNotFoundException {

		return "ok";

	}

	public String dogetdetails(HttpServletRequest req) throws EntityNotFoundException {
		Key matchKey = null;
		if (req.getParameter("key") != null) {
			matchKey = KeyFactory.stringToKey(req.getParameter("key"));
		} else {
			Long id = Long.valueOf(req.getParameter("id"));
			matchKey = KeyFactory.createKey(Match.class.getSimpleName(), id);
		}

		return getMatchDetailsJson(matchKey).toString();

	}

	private JSONObject getMatchDetailsJson(Key matchKey) {
		try {
			Match match = (Match) DBFacade.getInstance().get(matchKey, Match.class);
			if (match.getRanking() != null) {
				Ranking ranking = (Ranking) DBFacade.getInstance().get(match.getIdRanking(), Ranking.class);
				match.setRanking(ranking);
			}
			List<Entity> sets = DBFacade.getInstance().getAllSetsFrom(match);
			JSONArray setsArr = new JSONArray();
			for (Iterator iterator = sets.iterator(); iterator.hasNext();) {
				Entity entity = (Entity) iterator.next();
				Set set = new Set(entity);
				List<Entity> games = DBFacade.getInstance().getAllGamesFrom(set);
				JSONArray gamesArr = new JSONArray();
				for (Iterator iterator2 = games.iterator(); iterator2.hasNext();) {
					Entity entity2 = (Entity) iterator2.next();
					Game game = new Game(entity);
					gamesArr.put(game.toJSON());
				}
				JSONObject setJson = set.toJSON();
				setJson.put("games", gamesArr);
				setsArr.put(setJson);

			}
			Player p1 = (Player) DBFacade.getInstance().get(match.getIdPlayerOne(), Player.class);
			Player p2 = (Player) DBFacade.getInstance().get(match.getIdPlayerTwo(), Player.class);
			JSONObject json = match.toJSON();
			json.put("playerOne", p1.toJSON());
			json.put("playerTwo", p2.toJSON());
			if (match.getRanking() != null) {
				json.put("ranking", match.getRanking().toJSON());
			}
			json.put("sets", setsArr);
			return json;
		} catch (Exception e) {
			// TODO Auto-generated catch block

			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public String dogetset(HttpServletRequest req) throws EntityNotFoundException, EntityValidationException {
		Long id = Long.valueOf(req.getParameter("id"));
		Integer number = Integer.valueOf(req.getParameter("number"));
		Key matchKey = KeyFactory.createKey(Match.class.getSimpleName(), id);
		Match match = (Match) DBFacade.getInstance().get(matchKey, Match.class);

		List<Entity> sets = DBFacade.getInstance().getAllSetsFrom(match);
		Set set = new Set(sets.get(number));

		return set.toJSONString();
	}

	public String dodeleteset(HttpServletRequest req) throws EntityNotFoundException, EntityValidationException {
		Key setKey = null;
		if (req.getParameter("ik") != null) {
			setKey = KeyFactory.stringToKey(req.getParameter("ik"));
		}
		Set set = (Set) DBFacade.getInstance().get(setKey, Set.class);
		Match match = (Match) DBFacade.getInstance().get(setKey.getParent(), Match.class);
		resetMatch(match);
		match = (Match) DBFacade.getInstance().get(setKey.getParent(), Match.class);
		List<Entity> sets = DBFacade.getInstance().getAllSetsFrom(match);
		for (Iterator iterator = sets.iterator(); iterator.hasNext();) {
			Entity entity = (Entity) iterator.next();
			Integer number = new Set(entity).getNumber();
			if (number > set.getNumber()) {
				entity.setProperty("number", number - 1);
				DBFacade.getInstance().persist(entity);
			}
		}
		DBFacade.getInstance().delete(setKey);

		return getMatchDetailsJson(match.getKey()).toString();
	}

	public String doaddgame(HttpServletRequest req) throws EntityNotFoundException, EntityValidationException {

		return "";
	}

	public String doupdateset(HttpServletRequest req) throws EntityNotFoundException, EntityValidationException {
		Key setKey = null;
		if (req.getParameter("ik") != null) {
			setKey = KeyFactory.stringToKey(req.getParameter("ik"));
		}
		Match match = (Match) DBFacade.getInstance().get(setKey.getParent(), Match.class);
		resetMatch(match);
		Set set = (Set) DBFacade.getInstance().get(setKey, Set.class);
		set.fromRequest(req);
		setKey = DBFacade.getInstance().persist(set);
		set = (Set) DBFacade.getInstance().get(setKey, Set.class);
		return set.toJSONString();

	}

	private void resetMatch(Match match) throws EntityValidationException {
		if (MatchState.Completed.equals(match.getMatchState())) {
			match.setPending();
			DBFacade.getInstance().persist(match);

		}
	}

	public String doaddset(HttpServletRequest req) throws EntityNotFoundException, EntityValidationException {
		Key matchKey = null;
		if (req.getParameter("key") != null) {
			matchKey = KeyFactory.stringToKey(req.getParameter("key"));

		} else {
			Long id = Long.valueOf(req.getParameter("id"));
			matchKey = KeyFactory.createKey(Match.class.getSimpleName(), id);
		}

		Match match = (Match) DBFacade.getInstance().get(matchKey, Match.class);
		resetMatch(match);
		List<Entity> sets = DBFacade.getInstance().getAllSetsFrom(match);
		Set set = new Set(match, sets.size());
		Key setKey = DBFacade.getInstance().persist(set);
		set = (Set) DBFacade.getInstance().get(setKey, Set.class);
		return set.toJSONString();
	}

	protected long doPersist(HttpServletRequest req) throws EntityNotFoundException, EntityValidationException {
		Logger LOG = Logger.getLogger("TESTE");
		Match m = new Match(req);

		if (req.getParameter("id") != null) {
			Long id = Long.valueOf(req.getParameter("id"));
			Key matchKey = KeyFactory.createKey(Match.class.getSimpleName(), id);
			m = (Match) DBFacade.getInstance().get(matchKey, Match.class);
			LOG.log(Level.INFO, "Editando partida");
		}
		m.fromRequest(req);
		Player playerOne = null;
		if (req.getParameter("playerOneEmail") != null) {
			playerOne = DBFacade.getInstance().getPlayerByEmail(req.getParameter("playerOneEmail"));
			m.setIdPlayerOne(playerOne.getKey().getId());
		} else {
			LOG.log(Level.INFO, "Erro grave!!!!");
			throw new RuntimeException("Não veio email do jogador 1");

		}
		String name = req.getParameter("playerTwoName");

		try {
			if (req.getParameter("playerTwoEmail") != null) {
				String playerTwoEmail = req.getParameter("playerTwoEmail").toLowerCase();
				Player playerTwo = DBFacade.getInstance().getPlayerByEmail(playerTwoEmail);
				m.setIdPlayerTwo(playerTwo.getKey().getId());

				java.text.SimpleDateFormat dateFmt = new java.text.SimpleDateFormat("dd/MM");
				java.text.SimpleDateFormat horaFmt = new java.text.SimpleDateFormat("HH:mm");
				Date data = new Date(Long.parseLong(req.getParameter("date")));
				LOG.log(Level.INFO, "Enviando email para " + playerTwoEmail);

				new SendEmail().sendEmail("Olá " + name + ",<br>" + req.getParameter("playerOneEmail") + " lhe convidou para uma partida de tênis dia " + dateFmt.format(data) + " às " + horaFmt.format(data) + " pelo aplicativo ATVP, Associação de Tenistas Virtuais Pro.<br><a href=\"https://play.google.com/store/apps/details?id=com.ionicframework.atvpmobile663442\"> <img alt=\"Get it on Google Play\" src=\"https://developer.android.com/images/brand/pt-br_generic_rgb_wo_45.png\" /></a><br>",
						"Ranking de Tênis Virtual", playerTwoEmail, name);

				if (DBFacade.getInstance().getFriendShip(playerOne.getKey(), playerTwo.getKey()) == null && DBFacade.getInstance().getFriendShip(playerTwo.getKey(), playerOne.getKey()) == null) {
					LOG.log(Level.INFO, "Não são amigos ainda. Criando amizade");
					Friendship fship = new Friendship(playerOne.getKey(), playerTwo.getKey());
					DBFacade.getInstance().persist(fship);
				}

			}
		} catch (Exception e) {
			if (req.getParameter("forceInvitation") != null && "true".equals(req.getParameter("forceInvitation"))) {
				String playerTwoEmail = req.getParameter("playerTwoEmail").toLowerCase();
				Player inviter = DBFacade.getInstance().getPlayerByEmail(req.getParameter("playerOneEmail"));
				Invitation invitation = new Invitation(inviter.getKey(), playerTwoEmail);

				Key invitationKey = DBFacade.getInstance().persist(invitation);

				Player p = new Player(name, "", playerTwoEmail, "---");
				Key p2Key = DBFacade.getInstance().persist(p);
				m.setIdPlayerTwo(p2Key.getId());
				try {

					new SendEmail().sendEmail("Olá " + name + ",<br>" + req.getParameter("playerOneEmail") + " lhe convidou para uma partida de tênis e para participar do aplicativo ATVP, Associação de Tenistas Virtuais Pro. " + "<br><a href=\"https://play.google.com/store/apps/details?id=com.ionicframework.atvpmobile663442\"> <img alt=\"Get it on Google Play\" src=\"https://developer.android.com/images/brand/pt-br_generic_rgb_wo_45.png\" /></a>.<br>", "Ranking de Tênis Virtual",
							playerTwoEmail, name);
				} catch (UnsupportedEncodingException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			} else {
				throw new RuntimeException(e);
			}
		}
		return DBFacade.getInstance().persist(m).getId();

	}

	public JSONArray getListByPlayer(HttpServletRequest req) throws EntityNotFoundException, EntityValidationException {

		Long idPlayer = Long.valueOf(req.getParameter("idPlayer"));

		Iterable<Entity> matches = DBFacade.getInstance().getAllMatchesForPlayer(idPlayer);

		JSONArray list = getMatchEntityDetails(matches);

		return list;
	}

	public String dolistbyplayer(HttpServletRequest req) throws EntityNotFoundException, EntityValidationException {

		return getListByPlayer(req).toString();
	}

	public String dolistdetails(HttpServletRequest req) throws EntityNotFoundException, EntityValidationException {
		Iterable<Entity> rankings = null;
		if (req.getParameter("idRanking") != null && req.getParameter("idRanking").length() > 0) {
			Long id = Long.valueOf(req.getParameter("idRanking"));
			rankings = DBFacade.getInstance().queryMatchesFrom(id);
			JSONArray list = getMatchEntityDetails(rankings);
			return list.toString();
		} else if (req.getParameter("idPlayer") != null || req.getParameter("idPlayer").length() > 0) {
			try {
				System.out.println("Carregando de jogador " + req.getParameter("idPlayer"));
				return getListByPlayer(req).toString();
			} catch (EntityNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (EntityValidationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		} else {
			System.err.println("nenhuma info disponivel");
			JSONArray list = getMatchEntityDetails(null);
			return list.toString();
		}

	}

	private JSONArray getMatchEntityDetails(Iterable<Entity> matchEntities) {
		JSONArray list = new JSONArray();
		try {
			for (Iterator iterator = matchEntities.iterator(); iterator.hasNext();) {
				Entity type = (Entity) iterator.next();
				Match r = new Match(type);
				JSONObject match = r.toJSON();
				match.put("playerOne", DBFacade.getInstance().get(r.getIdPlayerOne(), Player.class).toJSON());
				match.put("playerTwo", DBFacade.getInstance().get(r.getIdPlayerTwo(), Player.class).toJSON());
				list.put(match);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return list;
	}

	protected JSONArray doList(HttpServletRequest req) {

		if (req.getParameter("idRanking") == null || req.getParameter("idRanking").length() == 0) {
			try {

				return getListByPlayer(req);
			} catch (EntityNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (EntityValidationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		} else {
			Long id = Long.valueOf(req.getParameter("idRanking"));

			Iterable<Entity> rankings = DBFacade.getInstance().queryMatchesFrom(id);
			JSONArray list = new JSONArray();
			for (Iterator iterator = rankings.iterator(); iterator.hasNext();) {
				Entity type = (Entity) iterator.next();
				Match r = new Match(type);
				list.put(r.toJSON());
			}
			return list;
		}
	}

}
