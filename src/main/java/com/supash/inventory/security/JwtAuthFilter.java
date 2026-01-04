package com.supash.inventory.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.supash.inventory.utils.JwtUtil;

import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

	private final JwtUtil jwtUtil;
	private final UserDetailsService userDetailsService;

	public JwtAuthFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
		this.jwtUtil = jwtUtil;
		this.userDetailsService = userDetailsService;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		String token = extractJwtFromCookie(request);

		if (token != null && jwtUtil.isTokenValid(token)) {
			String username = jwtUtil.extractUsername(token);

			UserDetails userDetails = userDetailsService.loadUserByUsername(username);

			UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails,
					null, userDetails.getAuthorities());

			SecurityContextHolder.getContext().setAuthentication(authentication);
		}

		filterChain.doFilter(request, response);
	}

	private String extractJwtFromCookie(HttpServletRequest request) {
		if (request.getCookies() == null)
			return null;

		for (Cookie cookie : request.getCookies()) {
			if ("jwt".equals(cookie.getName())) {
				return cookie.getValue();
			}
		}
		return null;
	}
}

