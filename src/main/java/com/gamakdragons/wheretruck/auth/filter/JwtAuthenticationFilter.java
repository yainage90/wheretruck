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
import com.gamakdragons.wheretruck.common.SearchResultDto;

import org.springframework.http.HttpStatus;

public class JwtAuthenticationFilter implements Filter {

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse res = (HttpServletResponse) response;

		if(req.getMethod().equals("GET")) {
			chain.doFilter(req, res);
		}

		final String jwt = req.getHeader("jwt");

		try {
			String userId = JwtUtil.validate(jwt);
			req.setAttribute("userId", userId);
		} catch(JwtException e) {
			sendError(HttpStatus.UNAUTHORIZED, res);
			return;
		} 

		chain.doFilter(req, res);
	}

	private void sendError(HttpStatus status, HttpServletResponse res) throws IOException {
		res.setStatus(status.value());
        res.setContentType("application/json");
		res.sendError(status.value(), "UNAUTHORIZED");
	}
}
