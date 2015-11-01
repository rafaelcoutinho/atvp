package com.coutinho.atvp.entities;

import java.util.Map;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletRequest;

import com.coutinho.atvp.db.DBFacade;
import com.google.appengine.api.datastore.Entity;

public class Match extends DBObject<Match> {
	Ranking ranking;
	Long idPlayerOne;
	Long idPlayerTwo;

	Integer totalSetsPlayerOne;
	Integer totalSetsPlayerTwo;

	public Integer getTotalSetsPlayerOne() {
		return totalSetsPlayerOne;
	}

	public void setTotalSetsPlayerOne(Integer totalSetsPlayerOne) {
		this.totalSetsPlayerOne = totalSetsPlayerOne;
	}

	public Integer getTotalSetsPlayerTwo() {
		return totalSetsPlayerTwo;
	}

	public void setTotalSetsPlayerTwo(Integer totalSetsPlayerTwo) {
		this.totalSetsPlayerTwo = totalSetsPlayerTwo;
	}

	Long date;
	Long winnerId;
	MatchState state = null;
	private Long idRanking;

	public void setIdRanking(Long idRanking) {
		this.idRanking = idRanking;
	}

	public Long getWinnerId() {
		return this.winnerId;
	}

	public void setWinnerId(Long winnerId) {
		this.winnerId = winnerId;
	}

	public Long getIdRanking() {
		return this.idRanking;

	}

	protected Object convertProp(String propName, Class propClass) {
		if (propName.equals("matchState")) {
			return state.name();
		} else {
			return super.convertProp(propName, propClass);
		}
	}

	public MatchState getMatchState() {
		return this.state;
	}

	public void setMatchState(MatchState state) {
		this.state = state;
	}

	public Match(Entity entity) {
		super(entity);
		if (entity.getProperty("matchState") == null) {
			this.state = MatchState.Pending;
		} else {
			this.state = MatchState.valueOf((String) entity
					.getProperty("matchState"));
		}
	}

	public Match(HttpServletRequest req) {
		super(req);
		if (req.getAttribute("matchState") == null) {
			state = MatchState.Pending;
		}
	}

	public Match(Ranking ranking, Long idPlayerOne, Long idPlayerTwo, Long date) {
		super();
		state = MatchState.Pending;
		this.ranking = ranking;
		this.idPlayerOne = idPlayerOne;
		this.idPlayerTwo = idPlayerTwo;
		this.date = date;
		this.totalSetsPlayerOne = 0;
		this.totalSetsPlayerTwo = 0;
	}

	public Long getIdPlayerOne() {
		return this.idPlayerOne;
	}

	public void setIdPlayerOne(Long idPlayerOne) {
		this.idPlayerOne = idPlayerOne;
	}

	public Long getIdPlayerTwo() {
		return this.idPlayerTwo;
	}

	public void setIdPlayerTwo(Long idPlayerTwo) {
		this.idPlayerTwo = idPlayerTwo;
	}

	public Long getDate() {
		return this.date;
	}

	public void setDate(Long date) {
		this.date = date;
	}

	public Ranking getRanking() {
		return ranking;
	}

	public void setRanking(Ranking ranking) {
		this.ranking = ranking;
	}

	@Override
	public void fromEntity(Map m) {

		super.fromEntity(m);

		try {
			if (getKey() != null) {
				if (this.idRanking != null) {
					this.ranking = (Ranking) DBFacade.getInstance().get(
							this.idRanking, Ranking.class);
				}
			}
		} catch (EntityNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void setPending() {
		state = MatchState.Pending;
		winnerId = null;
		totalSetsPlayerOne = 0;
		totalSetsPlayerTwo = 0;

	}

}
