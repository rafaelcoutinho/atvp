package com.coutinho.atvp.entities;

import javax.servlet.http.HttpServletRequest;

import com.google.appengine.api.datastore.Entity;

public class Tournament extends DBObject<Tournament> {
	
	private Player manager;
	private int numberOfRounds = 1;
	private String name;
	private String description;

	public Tournament(Entity entity) {
		super(entity);

	}

	public Tournament(Player manager, String name) {
		this.manager = manager;
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

	public void setDescription(String description) {
		this.description = description;
	}

}
