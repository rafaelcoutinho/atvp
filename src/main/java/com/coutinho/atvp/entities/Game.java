package com.coutinho.atvp.entities;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.coutinho.atvp.db.DBFacade;
import com.coutinho.atvp.db.EntityNotFoundException;
import com.google.appengine.api.datastore.Entity;

public class Game extends DBObject<Game> {
	Set set;
	Long duration;
	Integer number;
	Integer playerOnePoints;
	Integer playerTwoPoints;
	Long winnerId;

	public Game(Entity entity) {
		super(entity);
	}

	public Game(HttpServletRequest req) {
		super(req);
	}

	public Game(Set set, Integer number) {
		super();
		this.set = set;
		this.number = number;
	}

	public Integer getPlayerTwoPoints() {
		return playerTwoPoints;
	}

	public void setPlayerTwoPoints(Integer playerTwoPoints) {
		this.playerTwoPoints = playerTwoPoints;
	}

	public Long getDuration() {
		return duration;
	}

	public void setDuration(Long duration) {
		this.duration = duration;
	}

	public Integer getNumber() {
		return number;
	}

	public void setNumber(Integer number) {
		this.number = number;
	}

	public Integer getPlayerOnePoints() {
		return playerOnePoints;
	}

	public void setPlayerOnePoints(Integer playerOnePoints) {
		this.playerOnePoints = playerOnePoints;
	}

	public Long getWinnerId() {
		return winnerId;
	}

	public void setWinnerId(Long winnerId) {
		this.winnerId = winnerId;
	}

	public Set getSet() {
		return set;
	}

	public void setSet(Set set) {
		this.set = set;
	}

	@Override
	public void fromEntity(Map m) {

		super.fromEntity(m);
		try {
			if (getKey() != null) {
				this.set = (Set) DBFacade.getInstance().get(
						getKey().getParent(), Set.class);
			}
		} catch (EntityNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
