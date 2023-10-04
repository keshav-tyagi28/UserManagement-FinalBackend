package com.osttra.controller;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.osttra.config.CustomResponseErrorHandler;
import com.osttra.entity.User;
import com.osttra.helper.JWTHelper;
import com.osttra.repository.temaDatabase.UserGroupRepository;
import com.osttra.repository.temaDatabase.UserRepository;
import com.osttra.service.CustomUserDetailsService;
import com.osttra.service.ExceptionManagementServiceImp;
//import com.osttra.service.ExceptionManagementServiceImp;
import com.osttra.service.UserDetailService;
import com.osttra.service.UserGroupDetailsService;
import com.osttra.to.CustomResponse;

@RequestMapping("/history")
@RestController
public class HistoryController {
	
	@Autowired
	UserDetailService userDetailsService;

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
	UserGroupDetailsService userGroupDetailsService;

	@Autowired
	UserRepository userRepository;

	@Autowired
	UserGroupController userGroupController;

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	ExceptionManagementServiceImp exceptionManagementServiceImp;

	@Autowired
	private CustomResponseErrorHandler customResponseErrorHandler;

	private String ip = "10.196.22.55:8080";
	
	private static final Logger logger = LoggerFactory.getLogger(HistoryController.class);

	
	@GetMapping("/claim/users/{username}")
	public ResponseEntity<?> getUserHistory(@PathVariable String username, HttpServletRequest request) {
		String externalApiUrl = "http://" + ip + "/engine-rest/history/user-operation?userId=" + username
				+ "&operationType=Claim";

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
			List<String> exceptionIds = new ArrayList<String>();
			for (JsonNode node : jsonNode) {
				String rootProcessInstanceId = node.get("rootProcessInstanceId").asText();
				String id = exceptionManagementServiceImp.fetchExceptionIdByProcessId(rootProcessInstanceId);
				if (id != "") {
					exceptionIds.add(id);
				}
			}

			CustomResponse<List<String>> successResponse = new CustomResponse<>(exceptionIds, "exception id's ",
					HttpStatus.OK.value(), request.getRequestURI());

			return new ResponseEntity<>(successResponse, HttpStatus.OK);

		} catch (IllegalArgumentException e) {
			logger.error("Bad Request: " + e.getMessage());
			CustomResponse<String> errorResponse = new CustomResponse<>("", "Bad Request: " + e.getMessage(),
					HttpStatus.BAD_REQUEST.value(), request.getServletPath());
			return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
			
		}catch (Exception e) {
			logger.error("Internal Server Error", e.getMessage());
			CustomResponse<User> errorResponse = new CustomResponse<>(null, "Internal Server Error",
					HttpStatus.INTERNAL_SERVER_ERROR.value(), request.getRequestURI());
			return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	
	
	@GetMapping("/complete/users/{username}")
	public ResponseEntity<?> getUserHistoryComplete(@PathVariable String username, HttpServletRequest request) {
		String externalApiUrl = "http://" + ip + "/engine-rest/history/user-operation?userId=" + username
				+ "&operationType=Complete";

		try {
			// Initialize the RestTemplate
			RestTemplate restTemplate = new RestTemplate();

			String jsonResponse = restTemplate.getForObject(externalApiUrl, String.class);

			ObjectMapper objectMapper = new ObjectMapper();

			JsonNode jsonNode = objectMapper.readTree(jsonResponse);

			List<String> exceptionIds = new ArrayList<String>();
			for (JsonNode node : jsonNode) {
				String rootProcessInstanceId = node.get("rootProcessInstanceId").asText();
				String id = exceptionManagementServiceImp.fetchExceptionIdByProcessId(rootProcessInstanceId);
				if (id != "") {
					exceptionIds.add(id);
				}
			}

			CustomResponse<List<String>> successResponse = new CustomResponse<>(exceptionIds, "exception id's ",
					HttpStatus.OK.value(), request.getRequestURI());

			return new ResponseEntity<>(successResponse, HttpStatus.OK);

		} catch (IllegalArgumentException e) {
			logger.error("Bad Request: " + e.getMessage());
			CustomResponse<String> errorResponse = new CustomResponse<>("", "Bad Request: " + e.getMessage(),
					HttpStatus.BAD_REQUEST.value(), request.getServletPath());
			return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
		}catch (Exception e) {
			logger.error("Internal Server Error", e.getMessage());
			CustomResponse<User> errorResponse = new CustomResponse<>(null, "Internal Server Error",
					HttpStatus.INTERNAL_SERVER_ERROR.value(), request.getRequestURI());
			return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}
	
	
	

	@GetMapping("/completed/usergroup/{usergroupid}")
	public ResponseEntity<?> getUserGroupHistoryCompleted(@PathVariable String usergroupid, HttpServletRequest request) {
		String externalApiUrl = "http://" + ip + "/engine-rest/history/task/count?finished=true&taskHadCandidateGroup=" + usergroupid;

		
		try {
			// Initialize the RestTemplate
			RestTemplate restTemplate = new RestTemplate();

			// Make a GET request to the external API and get the JSON response
			String jsonResponse = restTemplate.getForObject(externalApiUrl, String.class);

			// Initialize the ObjectMapper
			ObjectMapper objectMapper = new ObjectMapper();

			// Parse the JSON response
			JsonNode jsonNode = objectMapper.readTree(jsonResponse);

		    JsonNode countNode = jsonNode.get("count");
		    int count= countNode.intValue();


			CustomResponse<Integer> successResponse = new CustomResponse<>(count, " The Number of Completed Exceptions Of "+usergroupid,
					HttpStatus.OK.value(), request.getRequestURI());

			return new ResponseEntity<>(successResponse, HttpStatus.OK);

		} catch (IllegalArgumentException e) {
			logger.error("Bad Request: " + e.getMessage());
			CustomResponse<String> errorResponse = new CustomResponse<>("", "Bad Request: " + e.getMessage(),
					HttpStatus.BAD_REQUEST.value(), request.getServletPath());
			return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
		}catch (Exception e) {
			logger.error("Internal Server Error", e.getMessage());
			CustomResponse<User> errorResponse = new CustomResponse<>(null, "Internal Server Error",
					HttpStatus.INTERNAL_SERVER_ERROR.value(), request.getRequestURI());
			return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}
	
	
	

	@GetMapping("/assigned/usergroup/{usergroupid}")
	public ResponseEntity<?> getUserGroupHistoryAssigned(@PathVariable String usergroupid, HttpServletRequest request) {
		String externalApiUrl = "http://" + ip + "/engine-rest/history/task/count?&taskHadCandidateGroup=" + usergroupid;
		
		

		
		try {
			// Initialize the RestTemplate
			RestTemplate restTemplate = new RestTemplate();

			// Make a GET request to the external API and get the JSON response
			String jsonResponse = restTemplate.getForObject(externalApiUrl, String.class);

			// Initialize the ObjectMapper
			ObjectMapper objectMapper = new ObjectMapper();

			// Parse the JSON response
			JsonNode jsonNode = objectMapper.readTree(jsonResponse);

		    JsonNode countNode = jsonNode.get("count");
		    int count= countNode.intValue();


			CustomResponse<Integer> successResponse = new CustomResponse<>(count, " The Number of Assigned Exceptions Of "+usergroupid,
					HttpStatus.OK.value(), request.getRequestURI());

			return new ResponseEntity<>(successResponse, HttpStatus.OK);

		} catch (IllegalArgumentException e) {
			logger.error("Bad Request: " + e.getMessage());
			CustomResponse<String> errorResponse = new CustomResponse<>("", "Bad Request: " + e.getMessage(),
					HttpStatus.BAD_REQUEST.value(), request.getServletPath());
			return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
		}catch (Exception e) {
			logger.error("Internal Server Error", e.getMessage());
			CustomResponse<User> errorResponse = new CustomResponse<>(null, "Internal Server Error",
					HttpStatus.INTERNAL_SERVER_ERROR.value(), request.getRequestURI());
			return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}
}
