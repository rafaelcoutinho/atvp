package com.coutinho.atvp.server;

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
import com.coutinho.atvp.entities.Tournament;
import com.coutinho.atvp.exception.EntityValidationException;
import com.google.appengine.api.datastore.Key;

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
	@Produces(MediaType.APPLICATION_JSON)
	public List<Tournament> list(@QueryParam("id") long id) {
		return DBFacade.getInstance().queryManagersTournments(id);
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Tournament getResponse(Tournament tournament) {
		System.out.println(tournament);
		try {
			DBFacade.getInstance().persist(tournament);
		} catch (EntityValidationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return tournament;
	}

}
