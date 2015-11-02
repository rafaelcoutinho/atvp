package com.coutinho.atvp;

import org.restlet.Context;
import org.restlet.ext.jaxrs.JaxRsApplication;

public class RestletJaxRSApplicationConverter extends JaxRsApplication {

	public RestletJaxRSApplicationConverter(Context context) {
		super(context);
		this.add(new ATVPRestApp());
	}

}