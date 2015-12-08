package com.coutinho.atvp.entities;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.Transient;
import javax.servlet.http.HttpServletRequest;

import org.json.JSONObject;

import com.coutinho.atvp.exception.EntityValidationException;
import com.coutinho.atvp.server.BaseServlet;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

public abstract class DBObject<E> {
	Logger LOG = Logger.getLogger("DBObject");
	protected Key id;

	public Key getKey() {
		return id;
	}

	public void setKey(Key id) {
		this.id = id;
	}

	public DBObject() {

	}

	public DBObject(HttpServletRequest req) {
		fromRequest(req);
		if (req.getParameter("id") != null) {
			id = KeyFactory.createKey(getKind(), Long.valueOf(req.getParameter("id")));
		}
	}

	public void fromRequest(HttpServletRequest req) {
		Map<String, String> m = new HashMap<String, String>();
		Enumeration<String> ee = req.getParameterNames();

		while (ee.hasMoreElements()) {
			String elem = ee.nextElement();
			m.put(elem, req.getParameter(elem));
		}

		fromEntity(m);
	}

	public DBObject(Entity entity) {
		id = (entity.getKey());
		LOG.finest("id " + id);
		fromEntity(entity.getProperties());
	}

	public JSONObject toJSON() {

		Method[] fields = this.getClass().getMethods();
		JSONObject json = new JSONObject();
		for (int i = 0; i < fields.length; i++) {
			Method f = fields[i];
			try {
				if (f.getName().equals("getClass") || f.getName().equals("getKey")) {
					continue;
				}
				if (shouldAvoid(f.getName())) {
					continue;
				}
				if (f.getName().startsWith("get") && f.getParameterTypes().length == 0) {
					String propName = f.getName().substring(3, 4);
					propName = propName.toLowerCase() + f.getName().substring(4);
					Object obj = f.invoke(this);
					if (obj instanceof DBObject) {
						obj = ((DBObject) obj).toJSON();

					}
					json.put(propName, obj);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return json;

	}

	protected boolean shouldAvoid(String name) {

		return false;
	}

	public String toJSONString() {

		return toJSON().toString();
	}

	public void setProperties(Entity entity) {
		Method[] methods = this.getClass().getMethods();
		for (int i = 0; i < methods.length; i++) {
			Method f = methods[i];
			try {
				String mname = f.getName();
				if (mname.equals("getClass") || mname.equals("getId") || mname.equals("getKind") || mname.equals("getKey")) {
					continue;
				}

				if (f.getName().startsWith("get") && f.getParameterTypes().length == 0) {

					if (isTransient(f.getAnnotations())) {
						//
						continue;
					}
					String propName = f.getName().substring(3, 4);

					propName = propName.toLowerCase() + f.getName().substring(4);
					try {
						if (isTransient(this.getClass().getDeclaredField(propName).getAnnotations())) {

							continue;
						}
					} catch (NoSuchFieldException e) {
						LOG.log(Level.FINE, "No such field " + f.getName(), e);
					}
					Object obj = convertProp(propName, f.getReturnType());
					if (obj != null) {
						entity.setProperty(propName, obj);
					} else {
						entity.setProperty(propName, f.invoke(this));
					}
				}
			}

			catch (Exception e) {
				LOG.log(Level.WARNING, "Exception field " + f.getName(), e);

			}
		}
	}

	public boolean isTransient(Annotation[] as) {
		for (int j = 0; j < as.length; j++) {
			if (javax.persistence.Transient.class.equals(as[j].annotationType())) {

				return true;
			}
		}
		return false;
	}

	protected Object convertProp(String propName, Class propClass) {
		return null;
	}

	public void fromEntity(Map m) {

		for (Iterator iterator = m.keySet().iterator(); iterator.hasNext();) {
			String key = (String) iterator.next();
			if (key.equalsIgnoreCase("kind") || key.equalsIgnoreCase("key") || key.equalsIgnoreCase("ik")) {
				LOG.finest("nao deve setar o campo " + key);
				continue;
			}
			String mname = key.charAt(0) + "";

			mname = "set" + mname.toUpperCase() + key.substring(1);

			Method me = null;
			try {

				if (mname.equals("setId")) {
					LOG.finest("SEtnado id  " + mname);
					id = KeyFactory.createKey(getKind(), (Long) m.get(key));
					continue;
				}
				LOG.finest("buscando  " + mname);
				Method[] ms = this.getClass().getMethods();
				for (int i = 0; i < ms.length; i++) {
					if (ms[i].getName().equals(mname)) {
						me = ms[i];
						break;
					}
				}

				if (me == null) {
					LOG.finest("sem metodo?");
					throw new NoSuchMethodException(mname);
				} else {
					try {
						if ("undefined".equals((String) m.get(key))) {
							LOG.finest("valor indefinido para " + key);
							LOG.log(Level.FINER, "valor indefinido para " + key);

							continue;
						}
					} catch (Exception e) {

					}
					if (isHandledByChild(mname, m.get(key))) {

					} else if (me.getParameterTypes()[0].equals(String.class)) {
						LOG.finest("String... " + key + "=" + m.get(key));
						if (m.get(key) instanceof String[]) {
							me.invoke(this, ((String[]) m.get(key))[0]);
						} else {
							me.invoke(this, (String) m.get(key));
						}
					} else if (me.getParameterTypes()[0].equals(Integer.class)) {
						LOG.finest("Integer... " + key + "=" + m.get(key));
						Object val = m.get(key);
						Integer value = null;
						if (m.get(key) instanceof Integer) {
							value = (Integer) val;
						} else if (m.get(key) instanceof String) {
							value = Integer.valueOf((String) val);
						} else if (m.get(key) instanceof Long) {
							value = ((Long) val).intValue();
						}
						me.invoke(this, value);
					} else if (me.getParameterTypes()[0].equals(Long.class)) {
						LOG.finest("long... " + key + "=" + m.get(key));
						Object val = m.get(key);
						Long value = null;
						if (m.get(key) instanceof Long) {
							value = (Long) val;
						} else if (m.get(key) instanceof String) {
							value = Long.valueOf((String) val);
						}
						LOG.finest("invocou com " + value);
						me.invoke(this, value);
					} else if (m.get(key) instanceof DBObject) {
						LOG.log(Level.FINER, "podia pegar aqui");
						LOG.finest("m.get(key) instanceof DBObject");

					} else {
						LOG.finest("invocou pra setar " + key + "=" + m.get(key));
						me.invoke(this, m.get(key));
					}
				}
			} catch (NoSuchMethodException e) {
				LOG.log(Level.INFO, "Nao encontrou o set de " + mname + " de " + this.getKind());
				// BaseServlet.logException("Nao encontrou o set de " + mname +
				// " de " + this.getKind(), e);

			} catch (IllegalArgumentException e) {
				LOG.log(Level.WARNING, "IllegalArgumentException " + mname + " " + m.get(key));

				// BaseServlet.logException("IllegalArgumentException " + mname
				// + " " + m.get(key), e);
			} catch (Exception e) {
				LOG.log(Level.SEVERE, "Excecao  " + key + " " + m.get(key));

				// BaseServlet.logException("Excecao  " + key + " " +
				// m.get(key), e);
			}

		}

	}

	protected boolean isHandledByChild(String mname, Object object) {
		return false;
	}

	@Transient
	public String getIk() {
		if (id == null) {
			return null;
		}
		return KeyFactory.keyToString(id);
	}

	@Transient
	public Long getId() {
		if (id == null) {
			return null;
		}
		return id.getId();
	}

	public void validate(DatastoreService datastore) throws EntityValidationException {
		return;
	}

	@Transient
	public String getKind() {
		return getClass().getSimpleName();
	}
}
