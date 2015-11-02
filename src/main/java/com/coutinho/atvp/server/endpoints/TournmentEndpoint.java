package com.coutinho.atvp.server.endpoints;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.coutinho.atvp.db.DBFacade;
import com.coutinho.atvp.db.EntityNotFoundException;
import com.coutinho.atvp.entities.ScheduledMatch;
import com.coutinho.atvp.entities.Tournament;
import com.google.appengine.api.datastore.Entity;

@Path("tournament")
public class TournmentEndpoint {
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
