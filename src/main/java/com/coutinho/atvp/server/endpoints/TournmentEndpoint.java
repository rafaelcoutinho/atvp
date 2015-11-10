package com.coutinho.atvp.server.endpoints;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.coutinho.atvp.db.DBFacade;
import com.coutinho.atvp.db.EntityNotFoundException;
import com.coutinho.atvp.entities.Match;
import com.coutinho.atvp.entities.ScheduledMatch;
import com.coutinho.atvp.entities.Tournament;
import com.coutinho.atvp.entities.TournmentParticipant;
import com.coutinho.atvp.exception.EntityValidationException;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;

@Path("tournament")
public class TournmentEndpoint {
	Logger LOG = Logger.getLogger("TournmentEndpoint");
	@GET
	@Path("/{id:[0-9]+}")
	@Produces(MediaType.APPLICATION_JSON)
	public Tournament getResponse(@PathParam("id") Long id)
			throws EntityNotFoundException {
		Tournament t = (Tournament) DBFacade.getInstance().get(id,
				Tournament.class);

		return t;
	}

	@GET
	@Path("/{id:[0-9]+}/matches")
	@Produces(MediaType.APPLICATION_JSON)
	public List<ScheduledMatch> getMatches(@PathParam("id") Long id)
			throws EntityNotFoundException {
		Tournament t = (Tournament) DBFacade.getInstance().get(id,
				Tournament.class);

		return DBFacade.getInstance().getScheduledMatchesOfTournment(t.getId(),
				true);
	}

	@GET
	@Path("/{id:[0-9]+}/players")
	@Produces(MediaType.APPLICATION_JSON)
	public List<TournmentParticipant> getParticipants(@PathParam("id") Long id)
			throws EntityNotFoundException {
		Tournament t = (Tournament) DBFacade.getInstance().get(id,
				Tournament.class);

		return DBFacade.getInstance().getTournmentParticipants(t.getId(), true);
	}

	@POST
	@Path("/{id:[0-9]+}/players/{p:[0-9]+}")
	@Produces(MediaType.APPLICATION_JSON)
	public TournmentParticipant getParticipants(@PathParam("id") Long id,
			@PathParam("p") Long participantId) throws EntityNotFoundException {
		try {
			TournmentParticipant tp = DBFacade.getInstance()
					.getTournmentParticipants(id, participantId, false);
			if (tp == null) {
				tp = new TournmentParticipant(participantId, id);

				DBFacade.getInstance().persist(tp);

			}

			return tp;
		} catch (EntityValidationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@POST
	@Path("/{id:[0-9]+}/matches")
	@Produces(MediaType.APPLICATION_JSON)
	public ScheduledMatch saveMatches(@FormParam("smId") Long id,
			@FormParam("p1") Long p1, @FormParam("p2") Long p2) {
		try {
			LOG.info(id+" "+p1+" "+p2);
			ScheduledMatch t = (ScheduledMatch) DBFacade.getInstance().get(id,
					ScheduledMatch.class);
			LOG.info(t+" "+t.getScheduledDate());
			Match m = t.getMatch();
			if (t.getMatch() == null) {
				m = new Match(p1, p2, t.getScheduledDate());
				Key k = DBFacade.getInstance().persist(m);
				m.setKey(k);
				t.setMatch(m);
				DBFacade.getInstance().persist(t);
			} else {
				m.setIdPlayerOne(p1);
				m.setIdPlayerTwo(p2);
				DBFacade.getInstance().persist(m);
				t = (ScheduledMatch) DBFacade.getInstance().get(id,
						ScheduledMatch.class);
			}

			return t;

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;

	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<Tournament> list(@QueryParam("id") Long id) {
		if (id == null) {
			List<Tournament> ts = new ArrayList<Tournament>();
			for (Iterator<Entity> iterator = DBFacade.getInstance()
					.queryAll(Tournament.class).iterator(); iterator.hasNext();) {
				Entity tournament = (Entity) iterator.next();
				ts.add(new Tournament(tournament));

			}
			return ts;
		}
		return DBFacade.getInstance().queryManagersTournments(id);
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Tournament getResponse(Tournament tournament) {
		System.out.println(tournament);
		try {
			DBFacade.getInstance().persist(tournament);
			int numberOfRounds = tournament.getNumberOfRounds();
			System.out.println("numero de rounds " + numberOfRounds);
			List<ScheduledMatch> nextRoundMatches = new ArrayList<ScheduledMatch>();
			for (int i = 0; i < numberOfRounds; i++) {
				int totalScheduledGamesInRound = (int) Math.pow(2, (i));
				System.out.println(i + " tera " + totalScheduledGamesInRound
						+ " jogos...");
				List<ScheduledMatch> auxNextRoundMatches = new ArrayList<ScheduledMatch>();
				ScheduledMatch next = null;
				while (totalScheduledGamesInRound-- > 0) {
					System.out.println("next " + next + " "
							+ totalScheduledGamesInRound);
					if (!nextRoundMatches.isEmpty()
							&& totalScheduledGamesInRound % 2 != 0) {
						next = nextRoundMatches.remove(0);
					}
					System.out.println("next " + next);
					ScheduledMatch sm = new ScheduledMatch(tournament);
					sm.setRound(i);
					if (next != null) {
						sm.setNextScheduledMatch(next);
					}
					DBFacade.getInstance().persist(sm);
					auxNextRoundMatches.add(sm);
				}
				nextRoundMatches = auxNextRoundMatches;

			}
			System.out.println(tournament.getId());
			tournament = (Tournament) DBFacade.getInstance().getTournament(
					tournament.getId());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return tournament;
	}
}
