package com.coutinho.atvp.entities;

import javax.persistence.Transient;
import javax.servlet.http.HttpServletRequest;

import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import com.coutinho.atvp.server.TournmentJsonDeserializer;
import com.coutinho.atvp.server.TournmentJsonSerializer;
import com.google.appengine.api.datastore.Entity;

@JsonDeserialize(using = TournmentJsonDeserializer.class)
@JsonSerialize(using = TournmentJsonSerializer.class)
public class Tournament extends DBObject<Tournament> {

	private Long idManager;

	private int numberOfRounds = 1;
	private String name;
	private String description;

	@Transient
	transient private Player manager;

	public Tournament() {

	}

	public Tournament(Entity entity) {
		super(entity);

	}

	public Tournament(Long idManager, String name) {
		this.idManager = idManager;
		this.name = name;
	}

	public Tournament(HttpServletRequest req) {
		super(req);
	}

	public Player getManager() {
		return manager;
	}

	public void setManager(Player manager) {
		this.manager = manager;
	}

	public int getNumberOfRounds() {
		return numberOfRounds;
	}

	public void setNumberOfRounds(int numberOfRounds) {
		this.numberOfRounds = numberOfRounds;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public Long getIdManager() {
		return idManager;
	}

	public void setIdManager(Long idManager) {
		this.idManager = idManager;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
