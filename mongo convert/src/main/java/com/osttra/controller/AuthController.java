
package com.osttra.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.osttra.helper.JwtUtils;
import com.osttra.repository.temaDatabase.UserRepository;
import com.osttra.to.CustomUserDetails;
import com.osttra.to.JWTRequest;
import com.osttra.to.JWTResponse;





@RestController
@RequestMapping("/api/auth")
@CrossOrigin
public class AuthController {
	
	@Autowired
	  AuthenticationManager authenticationManager;

	  @Autowired
	  UserRepository userRepository;

	  @Autowired
	  PasswordEncoder encoder;

	  
	  @Autowired
	  JwtUtils jwtUtils;
	  
		private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

	  
	  @PostMapping("/signin")
	  public ResponseEntity<?> authenticateUser(@Valid @RequestBody JWTRequest loginRequest) {
	      try {
	          Authentication authentication = authenticationManager.authenticate(
	              new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

	          CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

	          SecurityContextHolder.getContext().setAuthentication(authentication);
	          String jwt = jwtUtils.generateToken(userDetails);

	          List<String> roles = userDetails.getAuthorities().stream()
	              .map(item -> item.getAuthority())
	              .collect(Collectors.toList());
	          
	          logger.info("User authenticated successfully.");

	          return ResponseEntity.ok(new JWTResponse(jwt, userDetails.getUsername(), roles));
	      } catch (BadCredentialsException e) {
	    	  
	            logger.error("Authentication failed: Incorrect username or password.", e.getMessage());

	          
	          Map<String, String> response = new HashMap<>();
	          response.put("message", "Incorrect username or password.");
	          return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
	      }
	  }

	  @PostMapping("/logout")
	    public ResponseEntity<String> logout() {
		  
		  try {
			  
			// Invalidate the user's authentication token
		        SecurityContextHolder.clearContext();
		        
		        return ResponseEntity.ok("Logged out successfully");
		        
			  
		  }catch (Exception e) {
	            logger.error("Logout failed", e);
	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Logout failed");
	        }
		  
	      
	        
	        
	    }
	  
	  

}