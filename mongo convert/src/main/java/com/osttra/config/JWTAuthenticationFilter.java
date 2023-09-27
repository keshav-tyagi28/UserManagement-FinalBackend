package com.osttra.config;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.osttra.helper.JWTHelper;
import com.osttra.service.CustomUserDetailsService;

@Component
public class JWTAuthenticationFilter extends OncePerRequestFilter {

//	
//	  if (!userDetails.getAuthorities().stream()
//    .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"))) 

    private static final Logger LOGGER = Logger.getLogger(JWTAuthenticationFilter.class.getName());

	@Autowired
	JWTHelper jwtHelper;

	@Autowired
	CustomUserDetailsService customUserDetailsService;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        LOGGER.info("Inside JWTAuthenticationFilter...");
		String tokenHeader = request.getHeader("Authorization");
		String token = null;
		String username = null;

		if (tokenHeader != null && tokenHeader.startsWith("Bearer ")) {

			token = tokenHeader.substring(7);

			username = jwtHelper.extractUsername(token);

			UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

			if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

				UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
						userDetails, null, userDetails.getAuthorities());

				usernamePasswordAuthenticationToken
						.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

				SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
			} else {
                LOGGER.warning("Authentication failed for token: " + token);
			}
			
		}
		filterChain.doFilter(request, response);
	}

}
