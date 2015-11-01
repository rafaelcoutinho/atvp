package com.coutinho.atvp.server;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

public class RestServletFilters implements Filter {

	public RestServletFilters() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void destroy() {

	}

	@Override
	public void doFilter(ServletRequest arg0, ServletResponse respRaw,
			FilterChain chain) throws IOException, ServletException {
		arg0.setCharacterEncoding("UTF-8");
		HttpServletResponse resp = (HttpServletResponse) respRaw;
		resp.addHeader("Access-Control-Allow-Headers",
				"Origin, X-Requested-With, Content-Type, Accept");
		resp.addHeader("Access-Control-Allow-Origin", "*");
		resp.addHeader("Access-Control-Allow-Methods",
				"POST, DELETE, PUT, GET,TRACE, OPTIONS");
		resp.setContentType("application/json; charset=UTF-8");
		resp.setCharacterEncoding("UTF-8");
		// resp.addHeader("Content-Type", );

		chain.doFilter(arg0, respRaw);

	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
		// TODO Auto-generated method stub

	}

}
