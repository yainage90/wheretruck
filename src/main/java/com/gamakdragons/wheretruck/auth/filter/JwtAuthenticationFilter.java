package com.gamakdragons.wheretruck.auth.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.gamakdragons.wheretruck.auth.exception.JwtException;
import com.gamakdragons.wheretruck.auth.service.JwtUtil;

import org.springframework.http.HttpStatus;

public class JwtAuthenticationFilter implements Filter {

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse res = (HttpServletResponse) response;

		final String jwt = req.getHeader("jwt");

		try {
			String userId = JwtUtil.validate(jwt);
			req.setAttribute("userId", userId);
		} catch(JwtException e) {
			res.sendError(HttpStatus.UNAUTHORIZED.value(), e.getMessage());
		} 

		chain.doFilter(req, res);
	}

	
}
