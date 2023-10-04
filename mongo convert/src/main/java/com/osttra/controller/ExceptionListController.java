package com.osttra.controller;



import java.util.List;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.HttpStatus;

import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.CrossOrigin;

import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.PatchMapping;

import org.springframework.web.bind.annotation.PathVariable;

import org.springframework.web.bind.annotation.PostMapping;

import org.springframework.web.bind.annotation.RequestBody;

import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;

import com.osttra.entity.TemaExceptionEntity;

import com.osttra.repository.temaDatabase.TemaExceptionRepository;

import com.osttra.service.ExceptionManagementServiceImp;

@RestController

@CrossOrigin

@RequestMapping("/api")

public class ExceptionListController {

	String ipAddress = "localhost";

	@Autowired

	ExceptionManagementServiceImp exceptionManagementServiceImp;

	@Autowired

	TemaExceptionRepository temaExceptionRepository;

	@Autowired

	RestTemplate restTemplate;

	@GetMapping("/getAllExceptions")

	public ResponseEntity<?> getAllTemaExceptionList() {

		try {

			List<TemaExceptionEntity> mongoData = exceptionManagementServiceImp.getAllTemaException();

			if (!mongoData.isEmpty()) {

				return new ResponseEntity<>(mongoData, HttpStatus.OK);

			} else {

				return new ResponseEntity<>("Data not available", HttpStatus.OK);

			}

		} catch (Exception e) {

			// log.error("An error occurred while retrieving data from Tema", e);

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)

					.body("An error occurred while retrieving data from Tema: " + e.getMessage());

		}

	}

	@PostMapping("/assignException")

	public ResponseEntity<String> assignExceptionToGroup(@RequestBody Map<String, String> assignGroup) {

		try {

			String response = exceptionManagementServiceImp.assignExceptionToGroup(assignGroup);

			return ResponseEntity.ok(response);

		} catch (Exception e) {

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)

					.body("Error occurred while processing the request: " + e.getMessage());

		}

	}

	@GetMapping("/getUserAssignedExceptions/{userId}")

	public ResponseEntity<?> getUserAssignedExceptions(@PathVariable String userId) {

		try {

			List<TemaExceptionEntity> matchingExceptions = exceptionManagementServiceImp

					.getUserAssignedExceptions(userId);

			if (matchingExceptions.isEmpty()) {

				return ResponseEntity.noContent().build();

			}

			return ResponseEntity.ok(matchingExceptions);

		} catch (Exception e) {

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)

					.body("Error occurred while processing the request: " + e.getMessage());

		}

	}

	@GetMapping("/getUserAssignedFourEyeCheckUpExceptions/{userId}")

	public ResponseEntity<?> getUserAssignedFourEyeCheckUpExceptions(@PathVariable String userId) {

		try {

			List<TemaExceptionEntity> matchingExceptions = exceptionManagementServiceImp

					.getUserAssignedFourEyeCheckUpExceptions(userId);

			if (matchingExceptions.isEmpty()) {

				return ResponseEntity.noContent().build();

			}

			return ResponseEntity.ok(matchingExceptions);

		} catch (Exception e) {

			e.printStackTrace();

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)

					.body("Error occurred while processing the request: " + e.getMessage());

		}

	}

	@GetMapping("/getUnclaimedPerformTask/{userId}")

	public ResponseEntity<?> getUnclaimedPerformTask(@PathVariable String userId) {

		try {

			List<TemaExceptionEntity> matchingExceptions = exceptionManagementServiceImp

					.getUnclaimedPerformTaskExceptions(userId);

			if (matchingExceptions.isEmpty()) {

				return ResponseEntity.noContent().build();

			}

			return ResponseEntity.ok(matchingExceptions);

		} catch (Exception e) {

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)

					.body("Error occurred while processing the request: " + e.getMessage());

		}

	}

	@GetMapping("/getUnclaimedFourEye/{userId}")

	public ResponseEntity<?> getUnclaimedFourEye(@PathVariable String userId) {

		try {

			List<TemaExceptionEntity> matchingExceptions = exceptionManagementServiceImp

					.getUnclaimedFourEyeExceptions(userId);

			if (matchingExceptions.isEmpty()) {

				return ResponseEntity.noContent().build();

			}

			return ResponseEntity.ok(matchingExceptions);

		} catch (Exception e) {

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)

					.body("Error occurred while processing the request: " + e.getMessage());

		}

	}

	@PostMapping("/claimUser")

	public ResponseEntity<?> claimExceptionByUser(@RequestBody Map<String, String> userExceptionIdMap) {

		ResponseEntity<String> response = exceptionManagementServiceImp.claimExceptionByUser(userExceptionIdMap);

		if (response.getStatusCode().is2xxSuccessful()) {

			return ResponseEntity.ok("Claimed exception successfully.");

		} else {

			return ResponseEntity.status(response.getStatusCode())

					.body("Failed to claim exception: " + response.getBody());

		}

	}

	@PostMapping("/completedFourEyeCheckUp")

	public ResponseEntity<?> completedFourEyeCheckUp(@RequestBody Map<String, String> exceptionId) {

		ResponseEntity<String> response = exceptionManagementServiceImp

				.completeFourEyeCheckUpTask(exceptionId.get("exceptionId"));

		if (response.getStatusCode().is2xxSuccessful()) {

			return ResponseEntity.ok("Group to Four Eye Check done !!!");

		} else {

			return ResponseEntity.status(response.getStatusCode())

					.body("Failed to complete Four Eye Check: " + response.getBody());

		}

	}

	@GetMapping("/get/{exceptionId}")

	public ResponseEntity<?> getExceptionDetail(@PathVariable String exceptionId) {

		try {

			TemaExceptionEntity exceptionDetails = exceptionManagementServiceImp.getExceptionDetails(exceptionId);

			if (exceptionDetails != null) {

				return ResponseEntity.ok(exceptionDetails);

			} else {

				return ResponseEntity.notFound().build();

			}

		} catch (Exception e) {

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)

					.body("Failed to retrieve exception details: " + e.getMessage());

		}

	}

	@GetMapping("/getHistory/{exceptionId}")

	public ResponseEntity<?> getExceptionHistory(@PathVariable String exceptionId) {

		try {

			List<Map<String, Object>> exceptionHistory = exceptionManagementServiceImp.getExceptionHistory(exceptionId);

			if (!exceptionHistory.isEmpty()) {

				return ResponseEntity.ok(exceptionHistory);

			} else {

				return ResponseEntity.notFound().build();

			}

		} catch (Exception e) {

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)

					.body("Failed to retrieve exception history: " + e.getMessage());

		}

	}

	@PostMapping("/groupToFourEyeCheck")

	public ResponseEntity<?> groupToFourEyeCheck(@RequestBody Map<String, String> request) {

		try {

			String responseMessage = exceptionManagementServiceImp.completeTask(request, 0);

			return ResponseEntity.ok(responseMessage);

		} catch (Exception e) {

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)

					.body("Error occurred while processing the request: " + e.getMessage());

		}

	}

	@PostMapping("/groupToEscalation")

	public ResponseEntity<?> groupToEscalation(@RequestBody Map<String, String> request) {

		try {

			String responseMessage = exceptionManagementServiceImp.completeTask(request, 1);

			System.out.println("inside groupToesc");

			return ResponseEntity.ok(responseMessage);

		} catch (Exception e) {

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)

					.body("Error occurred while processing the request: " + e.getMessage());

		}

	}

	@PostMapping("/escalationToFourEyeCheck")

	public ResponseEntity<?> escalationToFourEyeCheck(@RequestBody Map<String, String> request) {

		try {

			String responseMessage = exceptionManagementServiceImp.completeTask(request, 3);

			return ResponseEntity.ok(responseMessage);

		} catch (Exception e) {

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)

					.body("Error occurred while processing the request: " + e.getMessage());

		}

	}

	@PatchMapping("/updateResolutionCount/{exceptionId}")

	public ResponseEntity<String> updateResolutionCount(@PathVariable String exceptionId,

			@RequestBody int resolutionCount) {

		try {

			exceptionManagementServiceImp.updateResolutionCount(exceptionId, resolutionCount);

			return ResponseEntity.ok("Attribute updated successfully");

		} catch (RuntimeException e) {

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());

		}

	}

	@GetMapping("/escalationNumber")

	public ResponseEntity<?> getEscalationCount() {

		try {

			List<Map<String, Object>> monthlyData = exceptionManagementServiceImp.getEscalationCount();

			return ResponseEntity.ok(monthlyData);

		} catch (Exception e) {

			e.printStackTrace();

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)

					.body("Error occurred while processing the request: " + e.getMessage());

		}

	}

	@GetMapping("/resolutionTime")

	public ResponseEntity<?> getResolutionTime() {

		try {

			List<Map<String, Object>> monthlyData = exceptionManagementServiceImp.getDataForEveryMonth();

			return ResponseEntity.ok(monthlyData);

		} catch (Exception e) {

			e.printStackTrace();

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)

					.body("Error occurred while processing the request: " + e.getMessage());

		}

	}

	@GetMapping("/getUserId/{exceptionId}")

	public ResponseEntity<?> getUserId(@PathVariable String exceptionId) {

		String processId = exceptionManagementServiceImp.getProcessId(exceptionId);

		String taskId = exceptionManagementServiceImp.fetchTaskId(processId);

		String externalApiUrl = "http://localhost:8080/engine-rest/task/" + taskId + "/identity-links?type=assignee";

		RestTemplate restTemplate = new RestTemplate();

		JsonNode apiDataArray = restTemplate.getForObject(externalApiUrl, JsonNode.class);

		System.out.println(apiDataArray);

		if (apiDataArray.size() == 0) {

			return ResponseEntity.ok("NA");

		}

		JsonNode lastObject = apiDataArray.get(apiDataArray.size() - 1);

		if (lastObject.has("userId")) {

			String lastUserId = lastObject.get("userId").asText();

			return ResponseEntity.ok(lastUserId);

		}

		return ResponseEntity.ok("NA");

	}

	@GetMapping("/getUserGroupId/{exceptionId}")

	public ResponseEntity<?> getUserGroupId(@PathVariable String exceptionId) {

		String processId = exceptionManagementServiceImp.getProcessId(exceptionId);

		String taskId = exceptionManagementServiceImp.fetchTaskId(processId);

		String externalApiUrl = "http://localhost:8080/engine-rest/task/" + taskId + "/identity-links?type=candidate";

		RestTemplate restTemplate = new RestTemplate();

		JsonNode apiDataArray = restTemplate.getForObject(externalApiUrl, JsonNode.class);

		System.out.println(apiDataArray);

		if (apiDataArray.size() == 0) {

			return ResponseEntity.ok("NA");

		}

		JsonNode lastObject = apiDataArray.get(apiDataArray.size() - 1);

		if (lastObject.has("groupId")) {

			String lastUserId = lastObject.get("groupId").asText();

			return ResponseEntity.ok(lastUserId);

		}

		return ResponseEntity.ok("NA");

	}

	@GetMapping("/getUserGroups/{userId}")

	public JsonNode getUserGroups(@PathVariable String userId) {

		try {

			return exceptionManagementServiceImp.getUserGroups(userId);

		} catch (Exception e) {

			e.printStackTrace();

		}

		return null;

	}

}