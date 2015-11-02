package com.coutinho.atvp.entities;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Transient;

import com.google.appengine.api.datastore.Entity;

public class ScheduledMatch extends DBObject<ScheduledMatch> {

	@Transient
	Tournament tournment;

	@Transient
	Match match;
	@Transient
	ScheduledMatch nextScheduledMatch;

	@Column(nullable = true)
	Long matchId;
	@Column(nullable = true)
	Long nextScheduledMatchId;
	@Column(nullable = false)
	Long tournmentId;
	@Column(nullable = false)
	Integer round;
	Date scheduledDate;

	ScheduledMatch() {

	}

	public ScheduledMatch(Entity entity) {
		super(entity);
	}

	public ScheduledMatch(Tournament t) {
		this.tournment = t;
		this.tournmentId = t.getId();

	}

	public Tournament getTournment() {
		return tournment;
	}

	public void setTournment(Tournament tournment) {
		this.tournment = tournment;
	}

	public Match getMatch() {
		return match;
	}

	public void setMatch(Match match) {
		this.match = match;
		this.matchId = match.getId();
	}

	public Date getScheduledDate() {
		return scheduledDate;
	}

	public void setScheduledDate(Date scheduledDate) {
		this.scheduledDate = scheduledDate;

	}

	public ScheduledMatch getNextScheduledMatch() {
		return nextScheduledMatch;
	}

	public void setNextScheduledMatch(ScheduledMatch nextScheduledMatch) {
		this.nextScheduledMatch = nextScheduledMatch;
		this.nextScheduledMatchId = nextScheduledMatch.getId();
	}

	public Integer getRound() {
		return round;
	}

	public void setRound(Integer round) {
		this.round = round;
	}

	public Long getMatchId() {
		return matchId;
	}

	public void setMatchId(Long matchId) {
		this.matchId = matchId;
	}

	public Long getNextScheduledMatchId() {
		return nextScheduledMatchId;
	}

	public void setNextScheduledMatchId(Long nextScheduledMatchId) {
		this.nextScheduledMatchId = nextScheduledMatchId;
	}

	public Long getTournmentId() {
		return tournmentId;
	}

	public void setTournmentId(Long tournmentId) {
		this.tournmentId = tournmentId;
	}

}
