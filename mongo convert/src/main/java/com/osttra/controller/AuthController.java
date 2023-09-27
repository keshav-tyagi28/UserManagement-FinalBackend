
package com.osttra.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.validation.Valid;

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

	          return ResponseEntity.ok(new JWTResponse(jwt, userDetails.getUsername(), roles));
	      } catch (BadCredentialsException e) {
	          
	          Map<String, String> response = new HashMap<>();
	          response.put("message", "Incorrect username or password.");
	          return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
	      }
	  }

	  @PostMapping("/logout")
	    public ResponseEntity<String> logout() {
	        // Invalidate the user's authentication token
	        SecurityContextHolder.clearContext();
	        
	        return ResponseEntity.ok("Logged out successfully");
	        
	        
	        
	    }
	  
	  

}