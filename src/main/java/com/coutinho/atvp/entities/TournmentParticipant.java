package com.coutinho.atvp.entities;

import javax.persistence.Transient;

import com.google.appengine.api.datastore.Entity;

public class TournmentParticipant extends DBObject<TournmentParticipant> {
	private Long tournmentId;
	private Long participantId;
	private Long date;
	@Transient
	private Player player;

	public TournmentParticipant(Entity entity) {
		super(entity);
	}

	public TournmentParticipant(Long idParticipant, Long tournmentId) {
		this.tournmentId = tournmentId;
		this.participantId = idParticipant;
		this.date = System.currentTimeMillis();
	}

	public Long getTournmentId() {
		return tournmentId;
	}

	public Long getParticipantId() {
		return participantId;
	}

	public void setParticipant(Player dbObject) {
		this.player = dbObject;

	}

	public Player getParticipant() {
		return player;
	}

	public Long getDate() {
		return date;
	}

	public void setTournmentId(Long tournmentId) {
		this.tournmentId = tournmentId;
	}

	public void setParticipantId(Long participantId) {
		this.participantId = participantId;
	}

	public void setDate(Long date) {
		this.date = date;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

}
