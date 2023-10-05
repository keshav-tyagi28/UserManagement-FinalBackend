package com.osttra.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.osttra.config.CustomResponseErrorHandler;
import com.osttra.entity.User;
import com.osttra.helper.JWTHelper;
import com.osttra.repository.temaDatabase.UserGroupRepository;
import com.osttra.repository.temaDatabase.UserRepository;
import com.osttra.service.CustomUserDetailsService;
import com.osttra.service.ExceptionManagementServiceImp;
import com.osttra.service.HistoryServiceImpl;
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
	HistoryServiceImpl historyService;

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

	@GetMapping("/claim/users/{username}")
	public ResponseEntity<?> getUserHistory(@PathVariable String username, HttpServletRequest request) {

		try {
			// Initialize the RestTemplate

			List<String> exceptionIds = historyService.getUserHistory(username);

			CustomResponse<List<String>> successResponse = new CustomResponse<>(exceptionIds, "exception id's ",
					HttpStatus.OK.value(), request.getRequestURI());

			return new ResponseEntity<>(successResponse, HttpStatus.OK);

		} catch (Exception e) {

			CustomResponse<User> errorResponse = new CustomResponse<>(null, "Internal Server Error",
					HttpStatus.INTERNAL_SERVER_ERROR.value(), request.getRequestURI());
			return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@GetMapping("/complete/users/{username}")
	public ResponseEntity<?> getUserHistoryComplete(@PathVariable String username, HttpServletRequest request) {

		try {

			List<String> exceptionIds = historyService.getUserHistoryComplete(username);
			CustomResponse<List<String>> successResponse = new CustomResponse<>(exceptionIds, "exception id's ",
					HttpStatus.OK.value(), request.getRequestURI());

			return new ResponseEntity<>(successResponse, HttpStatus.OK);

		} catch (Exception e) {

			CustomResponse<User> errorResponse = new CustomResponse<>(null, "Internal Server Error",
					HttpStatus.INTERNAL_SERVER_ERROR.value(), request.getRequestURI());
			return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@GetMapping("/completed/usergroup/{usergroupid}")
	public ResponseEntity<?> getUserGroupHistoryCompleted(@PathVariable String usergroupid,
			HttpServletRequest request) {

		try {

			int count = historyService.getUserGroupHistoryCompleted(usergroupid);

			CustomResponse<Integer> successResponse = new CustomResponse<>(count,
					" The Number of Completed Exceptions Of " + usergroupid, HttpStatus.OK.value(),
					request.getRequestURI());

			return new ResponseEntity<>(successResponse, HttpStatus.OK);

		} catch (Exception e) {

			CustomResponse<User> errorResponse = new CustomResponse<>(null, "Internal Server Error",
					HttpStatus.INTERNAL_SERVER_ERROR.value(), request.getRequestURI());
			return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@GetMapping("/assigned/usergroup/{usergroupid}")
	public ResponseEntity<?> getUserGroupHistoryAssigned(@PathVariable String usergroupid, HttpServletRequest request) {

		try {

			int count = historyService.getUserGroupHistoryAssigned(usergroupid);

			CustomResponse<Integer> successResponse = new CustomResponse<>(count,
					" The Number of Assigned Exceptions Of " + usergroupid, HttpStatus.OK.value(),
					request.getRequestURI());

			return new ResponseEntity<>(successResponse, HttpStatus.OK);

		} catch (Exception e) {

			CustomResponse<User> errorResponse = new CustomResponse<>(null, "Internal Server Error",
					HttpStatus.INTERNAL_SERVER_ERROR.value(), request.getRequestURI());
			return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@GetMapping("/escalated/groups")
	public ResponseEntity<?> getEscalatedExceptions(HttpServletRequest request) {

		try {
			Map<String, Integer> groupsescalated = historyService.getEscalated();

			groupsescalated.remove("other");

			CustomResponse<Map<String, Integer>> successResponse = new CustomResponse<>(groupsescalated,
					" exceptions escalated by each group ", HttpStatus.OK.value(), request.getRequestURI());

			return new ResponseEntity<>(successResponse, HttpStatus.OK);

		} catch (Exception e) {

			CustomResponse<User> errorResponse = new CustomResponse<>(null, "Internal Server Error",
					HttpStatus.INTERNAL_SERVER_ERROR.value(), request.getRequestURI());
			return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

}
