package com.osttra.controller;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.SystemPropertyUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.osttra.config.CustomResponseErrorHandler;
import com.osttra.entity.User;
import com.osttra.entity.UserGroup;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.osttra.helper.JWTHelper;
import com.osttra.repository.temaDatabase.UserGroupRepository;
import com.osttra.repository.temaDatabase.UserRepository;
import com.osttra.service.CustomUserDetailsService;

import com.osttra.service.UserDetailServiceImpl;
import com.osttra.service.UserGroupDetailsServiceImpl;
import com.osttra.to.CustomResponse;
import com.osttra.to.JWTRequest;

import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;

@RestController
@RequestMapping("/users")
public class UserController {

	
	@Autowired
	UserDetailServiceImpl userDetailsService;
	
	@Autowired
	AuthenticationManager authenticationManager;
	
	@Autowired
	CustomUserDetailsService customUserDetailsService;
	
	@Autowired
	UserGroupRepository userGroupRepository;
	
	@Autowired
	JWTHelper jwtHelper;
	@Autowired
	BCryptPasswordEncoder bCryptPasswordEncoder;
	

	@Autowired
	UserGroupDetailsServiceImpl userGroupDetailsService;
	
	@Autowired
	UserRepository userRepository;
	
	@Autowired
	UserGroupController userGroupController;

	 @Autowired
	private RestTemplate restTemplate;
	 
	 
	 @Autowired
	    private CustomResponseErrorHandler customResponseErrorHandler;
	 
	 private String ip="10.196.22.55:8080";


	
	// register user can send usergroup id's as array  
	 @PostMapping("/registeruser")
	 public ResponseEntity<Object> addUser(@RequestBody User user, HttpServletRequest request) {
	     try {
	        
	    	 
	    	 if (userRepository.existsById(user.getUsername())) {
	             // Username is already taken, return a response indicating it's not allowed
	             CustomResponse<String> errorResponse = new CustomResponse<>("", "Duplicate User", 409, request.getServletPath());
	             return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
	         }
	         
	    	 
	        
	         ObjectMapper objectMapper = new ObjectMapper();
	         ObjectNode jsonObject = objectMapper.createObjectNode();

	         ObjectNode profileObject = objectMapper.createObjectNode();
	         profileObject.put("id", user.getUsername());
	         profileObject.put("firstName", user.getFirstName());
	         profileObject.put("lastName", user.getLastName());
	         profileObject.put("email", user.getEmail());

	         ObjectNode credentialsObject = objectMapper.createObjectNode();
	         credentialsObject.put("password", user.getPassword());

	         jsonObject.set("profile", profileObject);
	         jsonObject.set("credentials", credentialsObject);

	        
	         	String jsonPayload = jsonObject.toString();
	    	  String externalApiUrl="http://"+ ip +"/engine-rest/user/create";
	    	  
   	 // ResponseEntity<String> responseEntity = userDetailsService.sendJsonToExternalApi(externalApiUrl, HttpMethod.POST, jsonPayload);


				String password = user.getPassword();

				String encodedPassword = this.bCryptPasswordEncoder.encode(password);

				user.setPassword(encodedPassword);
	    	
		         userDetailsService.saveUser(user);

	         for(String ugid:user.getUserGroupsId())
	         {
	        	
	        	 userGroupController.addUserGroup(user.getUsername(), ugid, request);  
	        	 
	         }
	        
	         
	         
	             CustomResponse<String> successResponse = new CustomResponse<>("", "User added successfully", HttpStatus.OK.value(), request.getServletPath());
	             return new ResponseEntity<>(successResponse, HttpStatus.OK);
	      
	     } catch (IllegalArgumentException e) {
	         CustomResponse<String> errorResponse = new CustomResponse<>("", "Bad Request: " + e.getMessage(), HttpStatus.BAD_REQUEST.value(), request.getServletPath());
	         return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
	     }
	     catch (Exception e) {
	         CustomResponse<String> errorResponse = new CustomResponse<>("", "Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR.value(), request.getServletPath());
	         return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
	     }
	 }

	 
	 
	 	//send all the users present
		@GetMapping(value = "/allusers", produces = "application/json")
	    @ResponseBody
	    public ResponseEntity<CustomResponse<List<User>>> getAllUsers(HttpServletRequest request) {
			try {
	            List<User> users = userDetailsService.getAllUser();
	            
	            // Create a success response with a CustomResponse
	            CustomResponse<List<User>> successResponse = new CustomResponse<>(
	                users,
	                "Listed all users",
	                HttpStatus.OK.value(),
	                request.getServletPath()
	            );
	            
	            return ResponseEntity.status(HttpStatus.OK).body(successResponse);
	            
	        } catch (IllegalArgumentException e) {
	            CustomResponse<List<User>> errorResponse = new CustomResponse<>(
	                    null, // Set the data to null or an empty list in case of an error
	                    "Bad Request: " + e.getMessage(),
	                    HttpStatus.BAD_REQUEST.value(),
	                    request.getRequestURI() // Get the request path
	                );
	                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);

	            } catch (Exception e) {
	                CustomResponse<List<User>> errorResponse = new CustomResponse<>(
	                    null, // Set the data to null or an empty list in case of an error
	                    "Internal Server Error",
	                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
	                    request.getRequestURI() // Get the request path
	                );
	                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
	            }
	    }
	    
	    
	    

	    
	    
	    //update a user using username
		
		@PutMapping("/update/{username}")
		public ResponseEntity<?> updateUser(@PathVariable String username, @RequestBody User updatedUser, HttpServletRequest request) {
		    try {
		        // Check if the user exists
		        User existingUser = userDetailsService.getUserById(username);

		        if (existingUser == null) {
		            // Return a 404 Not Found response
		            CustomResponse<String> errorResponse = new CustomResponse<>("", "User not found", HttpStatus.NOT_FOUND.value(), request.getServletPath());
		            return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
		        }

		        // Update the user's information
		        existingUser.setFirstName(updatedUser.getFirstName());
		        existingUser.setLastName(updatedUser.getLastName());
		        existingUser.setEmail(updatedUser.getEmail());

		        // Prepare the JSON data to send to the external API
		        ObjectMapper objectMapper = new ObjectMapper();
		        ObjectNode jsonObject = objectMapper.createObjectNode();
		        
		        jsonObject.put("id", username);
		        jsonObject.put("firstName", updatedUser.getFirstName());
		        jsonObject.put("lastName", updatedUser.getLastName());
		        jsonObject.put("email", updatedUser.getEmail());
		       

		     
		        userDetailsService.saveUser(existingUser);

		        String externalApiUrl = "http://" + ip + "/engine-rest/user/" + username + "/profile";
		        String jsonPayload = jsonObject.toString();

		        ResponseEntity<String> responseEntity = userDetailsService.sendJsonToExternalApi(externalApiUrl, HttpMethod.PUT, jsonPayload);
	        
		        CustomResponse<String> successResponse = new CustomResponse<>("", "Updated Successfully", HttpStatus.OK.value(), request.getServletPath());
	            return new ResponseEntity<>(successResponse, HttpStatus.OK);
		       
		           
		        
		    } catch (IllegalArgumentException e) {
			        CustomResponse<String> errorResponse = new CustomResponse<>("", "Bad Request", HttpStatus.BAD_REQUEST.value(), request.getServletPath());
		        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
		    } 
		    
		    catch (Exception e) {	
		        CustomResponse<String> errorResponse = new CustomResponse<>("", "Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR.value(), request.getServletPath());
		        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
		    }
		}

	    
	    
	    
		@GetMapping("/{username}/groups")
		@ResponseBody
		public ResponseEntity<?> getUserGroups(@PathVariable String username, HttpServletRequest request) {
			
			try {
				
				User user = userDetailsService.getUserById(username);

				if (user == null) {
					
					CustomResponse<String> errorResponse = new CustomResponse<>("", "User not found", HttpStatus.NOT_FOUND.value(),request.getServletPath());
		            return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
		            
				}

				
				
				Set<UserGroup> userGroups = new HashSet<UserGroup>();

				for (String temp : user.getUserGroupsId()) {

					UserGroup userGroup = userGroupDetailsService.getUserGroupById(temp);

					userGroups.add(userGroup);

				}
				
				CustomResponse<Set<UserGroup>> successResponse = new CustomResponse<>(userGroups, "User groups displayed succesfully",  HttpStatus.OK.value(),request.getServletPath());
		        return new ResponseEntity<>(successResponse, HttpStatus.OK);
				
			} catch (IllegalArgumentException e) {

		        CustomResponse<String> errorResponse = new CustomResponse<>("", "Bad Request: " + e.getMessage(), HttpStatus.BAD_REQUEST.value(),request.getServletPath());
		        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
		        
		    } catch (Exception e) {

		        CustomResponse<String> errorResponse = new CustomResponse<>("", "Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR.value(),request.getServletPath());
		        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
		        
		    }

		}

	    
		// find a user using username
	    
		@GetMapping("finduser/{username}")
		@ResponseBody
		public ResponseEntity<?> getSpecificUser(@PathVariable String username, HttpServletRequest request) {

			try {

				User user = userDetailsService.getUserById(username);

				if (user == null) {

					CustomResponse<String> errorResponse = new CustomResponse<>("", "User not found",
							HttpStatus.NOT_FOUND.value(), request.getServletPath());

					return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);

				}

				CustomResponse<User> successResponse = new CustomResponse<>(user, "User found", HttpStatus.OK.value(),
						request.getServletPath());

				return new ResponseEntity<>(successResponse, HttpStatus.OK);

			} catch (IllegalArgumentException e) {

				CustomResponse<String> errorResponse = new CustomResponse<>("", "Bad Request: " + e.getMessage(),
						HttpStatus.BAD_REQUEST.value(), request.getServletPath());

				return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);

			} catch (Exception e) {

				CustomResponse<String> errorResponse = new CustomResponse<>("", "Internal Server Error",
						HttpStatus.INTERNAL_SERVER_ERROR.value(), request.getServletPath());

				return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);

			}

		}
	    
	    
	    
	    //delete a user
	    
		@DeleteMapping("/delete/{username}")
		public ResponseEntity<CustomResponse<User>> deleteUser(@PathVariable String username,
				HttpServletRequest request) {
			try {
				User userToDelete = userDetailsService.getUserById(username);

				if (userToDelete == null) {
					CustomResponse<User> errorResponse = new CustomResponse<>(null, "User not found",
							HttpStatus.NOT_FOUND.value(), request.getRequestURI());

					return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
				}

				for (String temp : userToDelete.getUserGroupsId()) {

					ResponseEntity<Object> result = userGroupController.removeUserGroup(username, temp, request);

				}

				userDetailsService.deleteUser(username);

				String externalApiUrl = "http://" + ip + "/engine-rest/user/" + username;

				ResponseEntity<String> responseEntity = userDetailsService.sendJsonToExternalApi(externalApiUrl,
						HttpMethod.DELETE, null);

				CustomResponse<User> successResponse = new CustomResponse<>(userToDelete, "User deleted successfully",
						HttpStatus.OK.value(), request.getRequestURI());

				return new ResponseEntity<>(successResponse, HttpStatus.OK);

			} catch (IllegalArgumentException e) {
				CustomResponse<User> errorResponse = new CustomResponse<>(null, "Bad Request: " + e.getMessage(),
						HttpStatus.BAD_REQUEST.value(), request.getRequestURI());

				return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);

			} catch (Exception e) {
				CustomResponse<User> errorResponse = new CustomResponse<>(null, "Internal Server Error",
						HttpStatus.INTERNAL_SERVER_ERROR.value(), request.getRequestURI());
				return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}
		
		
		
		@GetMapping("/history/{username}")
		public void getUserHistory(@PathVariable String username)
		{
			String externalApiUrl = "http://" + ip +"/engine-rest/history/user-operation?userId=" + username + "&operationType=Claim";
			
			 try {
		            // Initialize the RestTemplate
		            RestTemplate restTemplate = new RestTemplate();

		            // Make a GET request to the external API and get the JSON response
		            String jsonResponse = restTemplate.getForObject(externalApiUrl, String.class);

		            // Initialize the ObjectMapper
		            ObjectMapper objectMapper = new ObjectMapper();

		            // Parse the JSON response
		            JsonNode jsonNode = objectMapper.readTree(jsonResponse);
		            

		            // Traverse the array and extract rootProcessInstanceId
		            for (JsonNode node : jsonNode) {
		                String rootProcessInstanceId = node.get("rootProcessInstanceId").asText();
		                System.out.println("rootProcessInstanceId: " + rootProcessInstanceId);
		            }
		        } catch (Exception e) {
		            e.printStackTrace();
		        }
		}
		
		
		
		
		
		
		
		
		
		
		
		
		
	}
	




	    
	    
	    
	    
	    
	    
	    
	    
	    
	
	

