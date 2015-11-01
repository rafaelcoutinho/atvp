package com.coutinho.atvp.entities;

import java.util.Map;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletRequest;

import com.coutinho.atvp.db.DBFacade;
import com.google.appengine.api.datastore.Entity;

public class Set extends DBObject<Set> {
	Match match;
	Long duration;
	Integer number;
	Integer playerOneGames;
	Integer playerTwoGames;

	Long winnerId;

	public Set(Entity entity) {
		super(entity);
	}

	public Set(HttpServletRequest req) {
		super(req);
	}

	public Set(Match match, Integer number) {
		super();
		this.match = match;
		this.number = number;
		this.playerOneGames = 0;
		this.playerTwoGames = 0;
		this.duration = 0l;
	}

	public Match getMatch() {
		return this.match;
	}

	public void setMatch(Match match) {
		this.match = match;
	}

	public Long getDuration() {
		return duration;
	}

	public void setDuration(Long duration) {
		this.duration = duration;
	}

	public Integer getNumber() {
		return this.number;
	}

	public void setNumber(Integer number) {
		this.number = number;
	}

	public Integer getPlayerOneGames() {
		return this.playerOneGames;
	}

	public void setPlayerOneGames(Integer playerOneGames) {
		this.playerOneGames = playerOneGames;
	}

	public Integer getPlayerTwoGames() {
		return this.playerTwoGames;
	}

	public void setPlayerTwoGames(Integer playerTwoGames) {
		this.playerTwoGames = playerTwoGames;
	}

	public Long getWinnerId() {
		return this.winnerId;
	}

	public void setWinnerId(Long winnerId) {
		this.winnerId = winnerId;
	}

	@Override
	public void fromEntity(Map m) {

		super.fromEntity(m);
		try {
			if (getKey() != null) {
				this.match = (Match) DBFacade.getInstance().get(
						getKey().getParent(), Match.class);
			}
		} catch (EntityNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
