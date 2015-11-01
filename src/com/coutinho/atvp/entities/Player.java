package com.coutinho.atvp.entities;

import javax.servlet.http.HttpServletRequest;

import com.coutinho.atvp.exception.EntityValidationException;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;

public class Player extends DBObject<Player> {

	private Integer licenses;
	private String password;
	private String phone;

	private String name;
	private String fbId;
	private String nickname;
	private String email;
	private String imageProfile;

	public String getFbId() {
		return fbId;
	}

	public void setFbId(String fbId) {
		this.fbId = fbId;
	}

	public String getImageProfile() {
		if (imageProfile == null || imageProfile.length() == 0) {
			if (fbId != null && fbId.length() > 0) {
				return "http://graph.facebook.com/" + fbId
						+ "/picture?width=64&height=64";
			}
		}
		return imageProfile;
	}

	public void setImageProfile(String imageProfile) {
		this.imageProfile = imageProfile;
	}

	public String getPassword() {
		return password;
	}

	public Integer getLicenses() {
		return this.licenses;
	}

	public void setLicenses(Integer licenses) {
		this.licenses = licenses;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Player(Entity employee) {
		super(employee);

	}

	public Player(String name, String nickname, String email, String password) {
		super();
		this.name = name;
		this.nickname = nickname;
		this.email = email;
		this.password = password;
		this.licenses = 0;
		this.imageProfile = "";
	}

	public Player(HttpServletRequest req) {
		super(req);

	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getEmail() {
		return this.email;
	}

	public void setEmail(String email) {
		this.email = email.toLowerCase();
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	@Override
	public void validate(DatastoreService datastore)
			throws EntityValidationException {
		if (getEmail() == null || getEmail().isEmpty()) {
			throw new EntityValidationException("Email vazio");
		}
		if (getPassword() == null || getPassword().isEmpty()) {
			throw new EntityValidationException("Senha vazia");
		}
		Filter emailFilter = new FilterPredicate("email", FilterOperator.EQUAL,
				email);

		Query q = new Query(getKind()).setFilter(emailFilter);
		PreparedQuery pq = datastore.prepare(q);
		if (pq.asSingleEntity() != null
				&& (getId() == null || pq.asSingleEntity().getKey().getId() != getId())) {
			throw new EntityValidationException("Email existente");
		}

	}

}
