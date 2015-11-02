package com.coutinho.atvp;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

import com.coutinho.atvp.server.TournmentEndpoint;

public class ATVPRestApp extends Application {

	@Override
	public Set<Class<?>> getClasses() {
		Set<Class<?>> classes = new HashSet<Class<?>>();
		classes.add(TournmentEndpoint.class);
		return classes;
	}

}
