package com.coutinho.atvp.entities;

import javax.servlet.http.HttpServletRequest;

import com.google.appengine.api.datastore.Entity;

public class Ranking extends DBObject<Ranking> {
	Long idManager;
	Long created;
	Long end;
	String title;
	String description;

	public Ranking(Entity entity) {
		super(entity);
	}

	public Ranking(HttpServletRequest req) {
		super(req);
		created = System.currentTimeMillis();
	}

	public Ranking(long idManager, String title, String description) {
		super();
		this.idManager = idManager;
		this.title = title;
		this.description = description;
		created = System.currentTimeMillis();
	}

	public Long getIdManager() {
		return idManager;
	}

	public void setIdManager(Long idManager) {
		this.idManager = idManager;
	}

	public Long getCreated() {
		return created;
	}

	public void setCreated(Long created) {
		this.created = created;
	}

	public Long getEnd() {
		return end;
	}

	public void setEnd(Long end) {
		this.end = end;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
