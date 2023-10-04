
package com.osttra.service;

 

import java.io.IOException;

import java.net.URI;

import java.net.URLEncoder;

import java.net.http.HttpClient;

import java.net.http.HttpRequest;

import java.net.http.HttpResponse;

import java.text.SimpleDateFormat;

import java.util.ArrayList;

import java.util.Calendar;

import java.util.Collections;

import java.util.Date;

import java.util.HashMap;

import java.util.List;

import java.util.Map;

import java.util.Optional;

 

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.HttpEntity;

import org.springframework.http.HttpHeaders;

import org.springframework.http.HttpMethod;

import org.springframework.http.HttpStatus;

import org.springframework.http.MediaType;

import org.springframework.http.ResponseEntity;

import org.springframework.stereotype.Service;

import org.springframework.web.client.HttpStatusCodeException;

import org.springframework.web.client.RestTemplate;

 

import com.fasterxml.jackson.core.JsonProcessingException;

import com.fasterxml.jackson.databind.JsonNode;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.osttra.entity.SourceExceptionEntity;

import com.osttra.entity.TemaExceptionEntity;

import com.osttra.repository.sourceExceptionDatabase.SourceExceptionRepository;

import com.osttra.repository.temaDatabase.TemaExceptionRepository;

 

@Service

public class ExceptionManagementServiceImp implements ExceptionManagementService {

 

	String ipAddress = "localhost";

 

	@Autowired

	private SourceExceptionRepository sourceExceptionRepository;

	@Autowired

	private TemaExceptionRepository temaExceptionRepository;

	@Autowired

	RestTemplate restTemplate;

 

	public List<SourceExceptionEntity> getAllFromSource() {

		return sourceExceptionRepository.findAll();

	}

 

	public SourceExceptionEntity addExceptionInSource(SourceExceptionEntity sourceData) {

		return sourceExceptionRepository.save(sourceData);

	}

 

	public void deleteAllItemsSource() {

		try {

			sourceExceptionRepository.deleteAll();

		} catch (Exception e) {

			// log.error("An error occurred while deleting all items from Source

			// collection.", e);

			throw new RuntimeException("An error occurred while deleting all items.", e);

		}

	}

 

	public void deleteAllItemsTema() {

		try {

			temaExceptionRepository.deleteAll();

		} catch (Exception e) {

			throw new RuntimeException("Error while deleting Tema items: " + e.getMessage(), e);

		}

	}

 

	public void migrateData() {

		List<SourceExceptionEntity> sourceData = sourceExceptionRepository.findAll();

		// log.info("Source Data: {}", sourceData);

 

		for (SourceExceptionEntity sourceEntity : sourceData) {

			// log.debug("Source Entity: {}", sourceEntity);

 

			Optional<TemaExceptionEntity> existingEntityOptional = temaExceptionRepository

					.findById(sourceEntity.getExceptionId());

 

			if (!existingEntityOptional.isPresent()) {

				TemaExceptionEntity temaExceptionEntity = createTemaExceptionEntity(sourceEntity);

				String exceptionId = temaExceptionEntity.getExceptionId();

				String exceptionType = temaExceptionEntity.getExceptionType();

 

				// log.info("Creating Tema Exception Entity - ID: {}, Type: {}", exceptionId,

				// exceptionType);

				String processId = fetchProcessId(exceptionId, exceptionType);

 

				// log.info("Process ID: {}", processId);

				temaExceptionEntity.setProcessId(processId);

				temaExceptionRepository.save(temaExceptionEntity);

			}

		}

	}

 

	private TemaExceptionEntity createTemaExceptionEntity(SourceExceptionEntity sourceEntity) {

		TemaExceptionEntity temaExceptionEntity = new TemaExceptionEntity();

		temaExceptionEntity.setExceptionId(sourceEntity.getExceptionId());

		temaExceptionEntity.setTradeId(sourceEntity.getTradeId());

		temaExceptionEntity.setCounterParty(sourceEntity.getCounterParty());

		temaExceptionEntity.setTradeDate(sourceEntity.getTradeDate());

		temaExceptionEntity.setExceptionType(sourceEntity.getExceptionType());

		temaExceptionEntity.setResolutionSteps(sourceEntity.getResolutionSteps());

		temaExceptionEntity.setStatus(sourceEntity.getStatus());

		temaExceptionEntity.setPriority(sourceEntity.getPriority());

		temaExceptionEntity.setDescription(sourceEntity.getDescription());

		temaExceptionEntity.setCreatedBy(sourceEntity.getCreatedBy());

		temaExceptionEntity.setCreatedAt(sourceEntity.getCreatedAt());

		temaExceptionEntity.setUpdatedBy(sourceEntity.getUpdatedBy());

		temaExceptionEntity.setUpdatedAt(sourceEntity.getUpdatedAt());

		temaExceptionEntity.setAssign("ASSIGN");

		temaExceptionEntity.setResolutionCount(-1);

		return temaExceptionEntity;

	}

 

	public String fetchProcessId(String exceptionId, String exceptionType) {

		Map<String, Object> requestMap = new HashMap<>();

		requestMap.put("businessKey", exceptionId);

		Map<String, Object> variablesMap = new HashMap<>();

		Map<String, Object> exceptionTypeMap = new HashMap<>();

		exceptionTypeMap.put("value", exceptionType);

		exceptionTypeMap.put("type", "String");

		variablesMap.put("ExceptionType", exceptionTypeMap);

		requestMap.put("variables", variablesMap);

 

		// log.debug("JSON Request: {}", requestMap);

 

		ObjectMapper objectMapper = new ObjectMapper();

		String exceptionIdJson = null;

		try {

			exceptionIdJson = objectMapper.writeValueAsString(requestMap);

		} catch (JsonProcessingException e) {

			// log.error("Error converting request to JSON", e);

			throw new RuntimeException("Error converting request to JSON", e);

		}

 

		String externalApiUrl = "http://" + ipAddress

				+ ":8080/engine-rest/process-definition/key/ApprovalProcess/start";

 

		ResponseEntity<String> response = postJsonToExternalApi(externalApiUrl, exceptionIdJson);

		return extractProcessIdFromResponse(response.getBody());

	}

 

	public String extractProcessIdFromResponse(String responseBody) {

		ObjectMapper objectMapper = new ObjectMapper();

		JsonNode jsonResponse;

		try {

			jsonResponse = objectMapper.readTree(responseBody);

		} catch (Exception e) {

			// log.error("Error parsing JSON response", e);

			throw new RuntimeException("Error parsing JSON response", e);

		}

 

		String processId = jsonResponse.get("id").asText();

		return processId;

	}

 

	public TemaExceptionEntity getExceptionDetails(String exceptionId) {

		Optional<TemaExceptionEntity> exceptionOptional = temaExceptionRepository.findById(exceptionId);

 

		if (exceptionOptional.isPresent()) {

			TemaExceptionEntity exception = exceptionOptional.get();

			updateExceptionStatus(exception);

			return exception;

		} else {

			return null;

		}

	}

 

	public List<TemaExceptionEntity> getAllTemaException() {

		try {

			List<TemaExceptionEntity> exceptions = temaExceptionRepository.findAll();

			for (TemaExceptionEntity exception : exceptions) {

				updateExceptionStatus(exception);

			}

			List<TemaExceptionEntity> reversedExceptions = new ArrayList<>(exceptions);

			Collections.reverse(reversedExceptions);

			return reversedExceptions;

		} catch (Exception e) {

			// log.error("An error occurred while retrieving data from

			// TemaExceptionRepository", e);

			throw new RuntimeException("An error occurred while retrieving data from TemaExceptionRepository", e);

		}

	}

 

	public String assignExceptionToGroup(Map<String, String> assignGroup) {

		String processId = getProcessId(assignGroup.get("exceptionId"));

		String taskId = fetchTaskId(processId);

		String groupId = assignGroup.get("groupId");

		assignGroup.put("type", "candidate");

		assignGroup.remove("exceptionId");

		String externalApiUrl = "http://" + ipAddress + ":8080/engine-rest/task/" + taskId + "/identity-links";

		String assignGroupJson = mapToJson(assignGroup);

		ResponseEntity<String> response = postJsonToExternalApi(externalApiUrl, assignGroupJson);

		if (response.getStatusCode().is2xxSuccessful()) {

			updateCandidateGroup(taskId, groupId);

			deleteOtherUser(taskId);

			return taskId;

		} else {

			throw new RuntimeException("External API returned an error: " + response.getBody());

		}

	}

 

	public ResponseEntity<String> deleteOtherUser(String taskId) {

		try {

			String externalApiUrl = "http://" + ipAddress + ":8080/engine-rest/task/" + taskId

					+ "/identity-links/delete";

			String jsonString = "{ \"type\":\"candidate\",\"groupId\":\"other\"}";

			ResponseEntity<String> response = postJsonToExternalApi(externalApiUrl, jsonString);

 

			if (response.getStatusCode().is2xxSuccessful()) {

				return ResponseEntity.ok("exception from other block deleted");

			} else {

				return ResponseEntity.status(response.getStatusCode())

						.body("External API returned an error: " + response.getBody());

			}

		} catch (Exception e) {

			e.printStackTrace();

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)

					.body("Error occurred while processing the request");

		}

	}

 

	public ResponseEntity<String> updateCandidateGroup(String taskId, String groupId) {

		try {

			String externalApiUrl = "http://" + ipAddress + ":8080/engine-rest/task/" + taskId

					+ "/variables/candidateGroups";

 

			HttpHeaders headers = new HttpHeaders();

			headers.setContentType(MediaType.APPLICATION_JSON);

 

			String jsonString = "{\"value\":\"" + groupId + "\",\"type\":\"String\"}";

 

			ResponseEntity<String> response = restTemplate.exchange(externalApiUrl, HttpMethod.PUT,

					new HttpEntity<>(jsonString, headers), String.class);

 

			if (response.getStatusCode().is2xxSuccessful()) {

				return ResponseEntity.ok("Updated the group");

			} else {

				return ResponseEntity.status(response.getStatusCode())

						.body("External API returned an error: " + response.getBody());

			}

		} catch (Exception e) {

			e.printStackTrace();

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)

					.body("Error occurred while processing the request");

		}

	}

 

	public List<TemaExceptionEntity> getUserAssignedExceptions(String userId) {

		List<TemaExceptionEntity> matchingExceptions = new ArrayList<>();

 

		String externalApiUrl1 = "http://" + ipAddress + ":8080/engine-rest/task?assignee=" + userId

				+ "&name=Perform Task";

		String externalApiUrl2 = "http://" + ipAddress + ":8080/engine-rest/task?assignee=" + userId

				+ "&name=Escalation";

 

		JsonNode apiDataArray1 = restTemplate.getForObject(externalApiUrl1, JsonNode.class);

		JsonNode apiDataArray2 = restTemplate.getForObject(externalApiUrl2, JsonNode.class);

 

		matchingExceptions.addAll(processTaskData(apiDataArray1));

		matchingExceptions.addAll(processTaskData(apiDataArray2));

 

		return matchingExceptions;

	}

 

	private List<TemaExceptionEntity> processTaskData(JsonNode apiDataArray) {

		List<TemaExceptionEntity> matchingExceptions = new ArrayList<>();

 

		if (apiDataArray != null && apiDataArray.isArray()) {

			for (JsonNode item : apiDataArray) {

				if (item.has("processInstanceId")) {

					String processInstanceIdFromApi = item.get("processInstanceId").asText();

					TemaExceptionEntity matchingException = temaExceptionRepository

							.findByProcessId(processInstanceIdFromApi);

 

					if (matchingException != null) {

						updateExceptionStatus(matchingException);

						matchingExceptions.add(matchingException);

					}

				}

			}

		}

 

		return matchingExceptions;

	}

 

	public void updateExceptionStatus(TemaExceptionEntity exception) {

		String processId = exception.getProcessId();

		String externalApiUrl = "http://" + ipAddress + ":8080/engine-rest/history/activity-instance?processInstanceId="

				+ processId + "&sortBy=startTime&sortOrder=desc";

		ResponseEntity<String> response = restTemplate.getForEntity(externalApiUrl, String.class);

 

		String status = getExceptionStatus(response);

		exception.setStatus(status);

 

		String responseBody = response.getBody();

		if (responseBody != null) {

			ObjectMapper objectMapper = new ObjectMapper();

			try {

				JsonNode jsonArray = objectMapper.readTree(responseBody);

				if (jsonArray.isArray() && jsonArray.size() > 0) {

					// JsonNode firstObject = jsonArray.get(0);

					// JsonNode assigneeNode = firstObject.get("assignee");

					// String assignee = assigneeNode.asText();

					if (exception.getStatus().equals("Open")) {

						String api = "http://" + ipAddress

								+ ":8080/engine-rest/task/?candidateGroup=other&processInstanceId=" + processId;

						ResponseEntity<String> apiResponse = restTemplate.getForEntity(api, String.class);

						exception.setAssign(apiResponse.getBody().length() == 2 ? "Assigned" : "Assign");

					} else {

						exception.setAssign("Assigned");

					}

				}

			} catch (Exception e) {

				throw new RuntimeException("Error in parsing error response", e);

			}

		}

 

		temaExceptionRepository.save(exception);

	}

 

	public String getExceptionStatus(ResponseEntity<String> response) {

		String status = "Open";

		try {

			String responseBody = response.getBody();

 

			if (responseBody != null) {

				ObjectMapper objectMapper = new ObjectMapper();

				JsonNode jsonArray = objectMapper.readTree(responseBody);

 

				if (jsonArray.isArray() && jsonArray.size() > 0) {

					JsonNode firstObject = jsonArray.get(0);

					JsonNode activityNameNode = firstObject.get("activityName");

					JsonNode activityTypeNode = firstObject.get("activityType");

					JsonNode assigneeNode = firstObject.get("assignee");

					String activity = activityNameNode.asText();

					String activityType = activityTypeNode.asText();

					String assignee = assigneeNode.asText();

 

					if (activity.equals("Perform Task")) {

						if (assignee.equals("null")) {

							return "Open";

						} else {

							return "Pending";

						}

					} else if (activity.equals("Escalation")) {

						if (assignee.equals("null")) {

							return "Open";

						} else {

							return "Pending";

						}

					} else if (activity.equals("4-Eye check")) {

						return "Resolved";

					} else if (activityType.equals("noneEndEvent")) {

						return "Closed";

					}

				}

			}

		} catch (Exception e) {

			throw new RuntimeException("Error in parsing error response", e);

		}

 

		return status;

	}

 

	public List<TemaExceptionEntity> getUserAssignedFourEyeCheckUpExceptions(String userId) throws Exception {

 

		String externalApiUrl = "http://" + ipAddress + ":8080/engine-rest/task?assignee=" + userId

				+ "&name=4-eye check";

		RestTemplate restTemplate = new RestTemplate();

		JsonNode apiDataArray = restTemplate.getForObject(externalApiUrl, JsonNode.class);

		if (apiDataArray == null || !apiDataArray.isArray()) {

			throw new Exception("No exceptions found for user " + userId);

		}

 

		List<TemaExceptionEntity> matchingExceptions = new ArrayList<>();

 

		for (JsonNode item : apiDataArray) {

			if (item.has("processInstanceId")) {

				String processInstanceIdFromApi = item.get("processInstanceId").asText();

				TemaExceptionEntity matchingException = temaExceptionRepository

						.findByProcessId(processInstanceIdFromApi);

 

				if (matchingException != null) {

					updateExceptionStatus(matchingException);

					matchingExceptions.add(matchingException);

				}

			}

		}

 

		return matchingExceptions;

	}

 

	public List<TemaExceptionEntity> getUnclaimedPerformTaskExceptions(String userId) throws Exception {

		String initialApiUrl = "http://" + ipAddress + ":8080/engine-rest/group?member=" + userId;

		JsonNode apiDataArray = restTemplate.getForObject(initialApiUrl, JsonNode.class);

 

		if (apiDataArray == null || !apiDataArray.isArray()) {

			throw new Exception("No groups found for user " + userId);

		}

 

		List<TemaExceptionEntity> matchingExceptions = new ArrayList<>();

 

		for (JsonNode item : apiDataArray) {

			if (item.has("id")) {

				String groupId = item.get("id").asText();

 

				String groupApiUrl = "http://" + ipAddress + ":8080/engine-rest/task?candidateGroup=" + groupId;

				try {

					JsonNode apiDataArray2 = restTemplate.getForObject(groupApiUrl, JsonNode.class);

 

					for (JsonNode item2 : apiDataArray2) {

						if (item2.has("processInstanceId") && !(item2.get("name").asText()).equals("4-Eye check")) {

							String processInstanceIdFromApi = item2.get("processInstanceId").asText();

							TemaExceptionEntity matchingException = temaExceptionRepository

									.findByProcessId(processInstanceIdFromApi);

 

							if (matchingException != null) {

								matchingExceptions.add(matchingException);

								updateExceptionStatus(matchingException);

							}

						}

					}

				} catch (HttpStatusCodeException e) {

					throw new RuntimeException("Failed to fetch data", e);

				}

			}

		}

		

		return matchingExceptions;

	}

 

	public List<TemaExceptionEntity> getUnclaimedFourEyeExceptions(String userId) {

		List<TemaExceptionEntity> matchingExceptions = new ArrayList<>();

 

		String initialApiUrl = "http://" + ipAddress + ":8080/engine-rest/group?member=" + userId;

 

		try {

			JsonNode apiDataArray = restTemplate.getForObject(initialApiUrl, JsonNode.class);

 

			if (apiDataArray != null && apiDataArray.isArray()) {

				for (JsonNode item : apiDataArray) {

					if (item.has("id")) {

						String groupId = item.get("id").asText();

						String groupApiUrl = "http://" + ipAddress + ":8080/engine-rest/task?candidateGroup=" + groupId;

 

						try {

							JsonNode apiDataArray2 = restTemplate.getForObject(groupApiUrl, JsonNode.class);

 

							for (JsonNode item2 : apiDataArray2) {

								if (item2.has("processInstanceId")

										&& "4-Eye check".equals(item2.get("name").asText())) {

									String processInstanceIdFromApi = item2.get("processInstanceId").asText();

									TemaExceptionEntity matchingException = temaExceptionRepository

											.findByProcessId(processInstanceIdFromApi);

 

									if (matchingException != null) {

										matchingExceptions.add(matchingException);

										updateExceptionStatus(matchingException);

									}

								}

							}

						} catch (HttpStatusCodeException e) {

							throw new RuntimeException("Failed to fetch data for group: " + groupId, e);

						}

					}

				}

			}

		} catch (HttpStatusCodeException e) {

			throw new RuntimeException("Failed to fetch initial data for user: " + userId, e);

		}

 

		return matchingExceptions;

	}

 

	public ResponseEntity<String> claimExceptionByUser(Map<String, String> userExceptionIdMap) {

		try {

			String processId = getProcessId(userExceptionIdMap.get("exceptionId"));

			String taskId = fetchTaskId(processId);

			userExceptionIdMap.remove("exceptionId");

			String externalApiUrl = "http://" + ipAddress + ":8080/engine-rest/task/" + taskId + "/claim";

			String assignUserJson = mapToJson(userExceptionIdMap);

 

			ResponseEntity<String> response = postJsonToExternalApi(externalApiUrl, assignUserJson);

 

			if (response.getStatusCode().is2xxSuccessful()) {

				return ResponseEntity.ok("Data sent to Spring Boot and external API successfully");

			} else {

				return ResponseEntity.status(response.getStatusCode())

						.body("External API returned an error: " + response.getBody());

			}

		} catch (Exception e) {

			e.printStackTrace();

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)

					.body("Error occurred while processing the request");

		}

	}

 

	public ResponseEntity<String> completeFourEyeCheckUpTask(String exceptionId) {

		try {

			String processId = getProcessId(exceptionId);

			String taskId = fetchTaskId(processId);

			String externalApiUrl = "http://" + ipAddress + ":8080/engine-rest/task/" + taskId + "/complete";

 

			String jsonString = "{ \"variables\": { \"i\": { \"value\": 0 } } }";

 

			ResponseEntity<String> response = postJsonToExternalApi(externalApiUrl, jsonString);

 

			if (response.getStatusCode().is2xxSuccessful()) {

				return ResponseEntity.ok("Group to Four Eye Check done !!!");

			} else {

				return ResponseEntity.status(response.getStatusCode())

						.body("External API returned an error: " + response.getBody());

			}

		} catch (Exception e) {

			e.printStackTrace();

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)

					.body("Error occurred while processing the request");

		}

	}

 

	public List<Map<String, Object>> getExceptionHistory(String exceptionId) {

		Optional<TemaExceptionEntity> exceptionOptional = temaExceptionRepository.findById(exceptionId);

		TemaExceptionEntity exception = null;

 

		if (exceptionOptional.isPresent()) {

			exception = exceptionOptional.get();

		}

 

		String processId = exception.getProcessId();

		String externalApiUrl = "http://" + ipAddress + ":8080/engine-rest/history/task?processInstanceId=" + processId

				+ "&sortBy=startTime&sortOrder=asc";

		ResponseEntity<String> response = restTemplate.getForEntity(externalApiUrl, String.class);

		String responseBody = response.getBody();

 

		ObjectMapper objectMapper = new ObjectMapper();

		List<Map<String, Object>> historyList = new ArrayList<>();

 

		try {

			JsonNode jsonArray = objectMapper.readTree(responseBody);

 

			if (jsonArray.isArray()) {

				for (JsonNode jsonObject : jsonArray) {

					String taskName = jsonObject.get("name").asText();

					String taskId = jsonObject.get("id").asText();

					String startTime = jsonObject.get("startTime").asText();

					String endTime = jsonObject.get("endTime").asText();

 

					Map<String, Object> outerMap = new HashMap<>();

					outerMap.put("taskName", taskName);

					outerMap.put("startTime", startTime);

					Map<String, Object> extractedObject = getExceptionHistoryForUserGroup(processId, taskId);

					outerMap.put("groupId", extractedObject.get("groupId"));

					outerMap.put("groupTime", extractedObject.get("groupTime"));

					outerMap.put("userId", extractedObject.get("userId"));

					outerMap.put("userTime", extractedObject.get("userTime"));

					outerMap.put("endTime", endTime);

					historyList.add(outerMap);

				}

			}

		} catch (Exception e) {

			e.printStackTrace();

		}

		return historyList;

	}

 

	private Map<String, Object> getExceptionHistoryForUserGroup(String processId, String taskId) {

		Map<String, Object> resultMap = new HashMap<>();

		String candidateLinkUrl = "http://" + ipAddress

				+ ":8080/engine-rest/history/identity-link-log?rootProcessInstanceId=" + processId + "&taskId=" + taskId

				+ "&operationType=add&type=candidate" + "&sortBy=time&sortOrder=asc";

		ResponseEntity<String> response = restTemplate.getForEntity(candidateLinkUrl, String.class);

		String responseBody = response.getBody();

 

		try {

			ObjectMapper objectMapper = new ObjectMapper();

			JsonNode jsonArray = objectMapper.readTree(responseBody);

 

			if (jsonArray.isArray() && jsonArray.size() > 0) {

				JsonNode lastObject = jsonArray.get(jsonArray.size() - 1);

				String groupTime = lastObject.get("time").asText();

				String groupId = lastObject.get("groupId").asText();

				resultMap.put("groupTime", groupTime);

				resultMap.put("groupId", groupId);

			}

		} catch (Exception e) {

			e.printStackTrace();

		}

 

		String assigneeLinkUrl = "http://" + ipAddress

				+ ":8080/engine-rest/history/identity-link-log?rootProcessInstanceId=" + processId + "&taskId=" + taskId

				+ "&operationType=add&type=assignee";

		ResponseEntity<String> response2 = restTemplate.getForEntity(assigneeLinkUrl, String.class);

		String responseBody2 = response2.getBody();

 

		try {

			ObjectMapper objectMapper = new ObjectMapper();

			JsonNode jsonArray = objectMapper.readTree(responseBody2);

 

			if (jsonArray.isArray() && jsonArray.size() > 0) {

				JsonNode lastObject = jsonArray.get(jsonArray.size() - 1);

				String userTime = lastObject.get("time").asText();

				String userId = lastObject.get("userId").asText();

				resultMap.put("userTime", userTime);

				resultMap.put("userId", userId);

			}

		} catch (Exception e) {

			e.printStackTrace();

		}

 

		return resultMap;

	}

 

	public String completeTask(Map<String, String> request, int value) {

		try {

			String exceptionId = request.get("exceptionId");

			String processId = getProcessId(exceptionId);

			String taskId = fetchTaskId(processId);

			String externalApiUrl = "http://" + ipAddress + ":8080/engine-rest/task/" + taskId + "/complete";

 

			String jsonString = "{ \"variables\": { \"i\": { \"value\": " + value + " } } }";

			ResponseEntity<String> response = postJsonToExternalApi(externalApiUrl, jsonString);

 

			if (response.getStatusCode().is2xxSuccessful()) {

				return "Task completed successfully.";

			} else {

				return "Error: External API returned an error - " + response.getBody();

			}

		} catch (Exception e) {

			throw new RuntimeException("Error occurred while processing the request: " + e.getMessage(), e);

		}

	}

 

	public void updateResolutionCount(String exceptionId, int resolutionCount) {

		Optional<TemaExceptionEntity> optionalEntity = temaExceptionRepository.findById(exceptionId);

 

		if (optionalEntity.isPresent()) {

			TemaExceptionEntity entity = optionalEntity.get();

			entity.setResolutionCount(resolutionCount);

			temaExceptionRepository.save(entity);

		} else {

			throw new RuntimeException("Exception with ID " + exceptionId + " not found.");

		}

	}

 

//	public Map<String, Object> getEscalationCount(String start, String end) {

//		Map<String, Object> exceptionCountMap = new HashMap<>();

//

//		try {

//			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

//			Date startedAfter = dateFormat.parse(start);

//			Date startedBefore = dateFormat.parse(end);

//

//			String encodedStartedAfter = URLEncoder.encode(toISOString(startedAfter), "UTF-8");

//			String encodedStartedBefore = URLEncoder.encode(toISOString(startedBefore), "UTF-8");

//

//			String url = "http://localhost:8080/engine-rest/history/task/count";

//			URI uri1 = URI.create(url + "?startedAfter=" + encodedStartedAfter + "&taskName=Escalation"

//					+ "&startedBefore=" + encodedStartedBefore);

//			long escalationCount = sendHttpRequestAndGetCount(uri1);

//			exceptionCountMap.put("escalationCount", escalationCount);

//

//			String taskName = URLEncoder.encode("Perform Task", "UTF-8");

//			URI uri2 = URI.create(url + "?startedAfter=" + encodedStartedAfter + "&taskName=" + taskName

//					+ "&startedBefore=" + encodedStartedBefore);

//			long totalCount = sendHttpRequestAndGetCount(uri2);

//			exceptionCountMap.put("total", totalCount);

//

//			String url3 = "http://localhost:8080/engine-rest/history/process-instance/count";

//			URI uri3 = URI.create(url3 + "?startedAfter=" + encodedStartedAfter + "&startedBefore="

//					+ encodedStartedBefore + "&finished=true");

//			long completedCount = sendHttpRequestAndGetCount(uri3);

//			exceptionCountMap.put("totalCompleted", completedCount);

//

//		} catch (Exception e) {

//			exceptionCountMap.put("error", "An error occurred while fetching Total count.");

//			e.printStackTrace();

//		}

//

//		return exceptionCountMap;

//	}

//

//	private Long sendHttpRequestAndGetCount(URI uri) throws IOException, InterruptedException {

//

//		HttpRequest request = HttpRequest.newBuilder().uri(uri).GET().build();

//		HttpClient client = HttpClient.newHttpClient();

//		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

//		ObjectMapper objectMapper = new ObjectMapper();

//		JsonNode jsonResponse = objectMapper.readTree(response.body());

//		Long count = jsonResponse.get("count").asLong();

//		return count;

//	}

 

public List<Map<String, Object>> getEscalationCount() throws Exception {

		List<Map<String, Object>> monthlyData = new ArrayList<>();

		Calendar calendar = Calendar.getInstance();

		calendar.set(Calendar.DAY_OF_MONTH, 1);

		int currentYear = calendar.get(Calendar.YEAR);

		int currentMonth = calendar.get(Calendar.MONTH);

 

		for (int month = 0; month < 12; month++) {

			int loopMonth = (currentMonth - month + 12) % 12;

			int loopYear = currentYear;

 

			if (currentMonth - month < 0) {

				loopYear--;

			}

 

			Map<String, Object> exceptionCountMap = new HashMap<>();

 

			calendar.set(Calendar.YEAR, loopYear);

			calendar.set(Calendar.MONTH, loopMonth);

 

			Date currentMonthStart = calendar.getTime();

			calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));

			Date currentMonthEnd = calendar.getTime();

			calendar.add(Calendar.DAY_OF_MONTH, 1);

 

			String encodedStartedAfter = URLEncoder.encode(toISOString(currentMonthStart), "UTF-8");

			String encodedStartedBefore = URLEncoder.encode(toISOString(currentMonthEnd), "UTF-8");

 

			String url = "http://localhost:8080/engine-rest/history/task/count";

			URI uri1 = URI.create(url + "?startedAfter=" + encodedStartedAfter + "&taskName=Escalation"

					+ "&startedBefore=" + encodedStartedBefore);

 

			long escalationCount = sendHttpRequestAndGetCount(uri1);

			exceptionCountMap.put("escalationCount", escalationCount);

 

			String taskName = URLEncoder.encode("Perform Task", "UTF-8");

			URI uri2 = URI.create(url + "?startedAfter=" + encodedStartedAfter + "&taskName=" + taskName

					+ "&startedBefore=" + encodedStartedBefore);

			long totalCount = sendHttpRequestAndGetCount(uri2);

			exceptionCountMap.put("total", totalCount);

 

			String url3 = "http://localhost:8080/engine-rest/history/process-instance/count";

			URI uri3 = URI.create(url3 + "?startedAfter=" + encodedStartedAfter + "&startedBefore="

					+ encodedStartedBefore + "&finished=true");

			long completedCount = sendHttpRequestAndGetCount(uri3);

			exceptionCountMap.put("totalCompleted", completedCount);

			exceptionCountMap.put("monthYear", String.format("%02d-%d", loopMonth + 1, loopYear));

 

			monthlyData.add(exceptionCountMap);

		}

 

		return monthlyData;

	}

 

	private Long sendHttpRequestAndGetCount(URI uri) throws IOException, InterruptedException {

 

		HttpRequest request = HttpRequest.newBuilder().uri(uri).GET().build();

		HttpClient client = HttpClient.newHttpClient();

		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

		ObjectMapper objectMapper = new ObjectMapper();

		JsonNode jsonResponse = objectMapper.readTree(response.body());

		Long count = jsonResponse.get("count").asLong();

		return count;

	}

 

	public static String toISOString(Date date) {

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

		return dateFormat.format(date);

	}

 

//	public Map<String, Object> getresolutionTime(String start, String end) {

//		Map<String, Object> resolutionTime = new HashMap<>();

//

//		try {

//			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

//			Date startedAfter = dateFormat.parse(start);

//			Date startedBefore = dateFormat.parse(end);

//

//			String encodedStartedAfter = URLEncoder.encode(toISOString(startedAfter), "UTF-8");

//			String encodedStartedBefore = URLEncoder.encode(toISOString(startedBefore), "UTF-8");

//

//			String url = "http://localhost:8080/engine-rest/history/process-instance?";

//			URI uri = URI.create(url + "?startedAfter=" + encodedStartedAfter + "&taskName=Escalation"

//					+ "&startedBefore=" + encodedStartedBefore + "&finished=true");

//			resolutionTime = sendHttpRequestAndGetTime(uri);

//

//		} catch (Exception e) {

//			resolutionTime.put("error", "An error occurred while fetching Total count.");

//			e.printStackTrace();

//		}

//

//		return resolutionTime;

//	}

 

	//

//	private Map<String, Object> sendHttpRequestAndGetTime(URI uri) throws IOException, InterruptedException {

//		Map<String, Object> resolutionTime = new HashMap<>();

//		HttpRequest request = HttpRequest.newBuilder().uri(uri).GET().build();

//		HttpClient client = HttpClient.newHttpClient();

//		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

//		ObjectMapper objectMapper = new ObjectMapper();

//		JsonNode jsonArray = objectMapper.readTree(response.body());

//

//		long time = 0;

//		long completedExceptions = 0;

//		if (jsonArray.isArray()) {

//

//			for (JsonNode jsonObject : jsonArray) {

//				time = jsonObject.get("durationInMillis").asLong();

//				completedExceptions++;

//			}

//		}

//

//		double timeInHours = time / 3600000.0;

//		double avgTime = timeInHours / completedExceptions;

//

//		resolutionTime.put("resolutionTime", timeInHours);

//		resolutionTime.put("avgResolutionTime", avgTime);

//		resolutionTime.put("totalResolvedException", completedExceptions);

//

//		return resolutionTime;

//

//	}

 

	public List<Map<String, Object>> getDataForEveryMonth() throws Exception {

		List<Map<String, Object>> monthlyData = new ArrayList<>();

 

		Calendar calendar = Calendar.getInstance();

		calendar.set(Calendar.DAY_OF_MONTH, 1);

		int currentYear = calendar.get(Calendar.YEAR);

		int currentMonth = calendar.get(Calendar.MONTH);

 

		for (int month = 0; month < 12; month++) {

			int loopMonth = (currentMonth - month + 12) % 12;

			int loopYear = currentYear;

 

			if (currentMonth - month < 0) {

				loopYear--;

			}

 

			calendar.set(Calendar.YEAR, loopYear);

			calendar.set(Calendar.MONTH, loopMonth);

 

			Date currentMonthStart = calendar.getTime();

			calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));

 

			Date currentMonthEnd = calendar.getTime();

			calendar.add(Calendar.DAY_OF_MONTH, 1);

 

			String encodedCurrentMonthStart = URLEncoder.encode(toISOString(currentMonthStart), "UTF-8");

			String encodedCurrentMonthEnd = URLEncoder.encode(toISOString(currentMonthEnd), "UTF-8");

 

			String url = "http://localhost:8080/engine-rest/history/process-instance?";

			URI uri = URI.create(url + "?startedAfter=" + encodedCurrentMonthStart + "&taskName=Escalation"

					+ "&startedBefore=" + encodedCurrentMonthEnd + "&finished=true");

 

			Map<String, Object> resolutionTime = sendHttpRequestAndGetTime(uri, loopMonth, loopYear);

			monthlyData.add(resolutionTime);

 

//	        if (loopMonth == currentMonth && loopYear == currentYear) {

//	            // Move the current month data to the end

//	            monthlyData.remove(monthlyData.size() - 1);

//	            monthlyData.add(resolutionTime);

//	        }

		}

 

		return monthlyData;

	}

 

	private Map<String, Object> sendHttpRequestAndGetTime(URI uri, int month, int year)

			throws IOException, InterruptedException {

		Map<String, Object> resolutionTime = new HashMap<>();

 

		try {

			HttpRequest request = HttpRequest.newBuilder().uri(uri).GET().build();

			HttpClient client = HttpClient.newHttpClient();

			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

 

			if (response.statusCode() == 200) {

				ObjectMapper objectMapper = new ObjectMapper();

				JsonNode jsonArray = objectMapper.readTree(response.body());

 

				long time = 0;

				long completedExceptions = 0;

 

				if (jsonArray.isArray()) {

					for (JsonNode jsonObject : jsonArray) {

						time += jsonObject.get("durationInMillis").asLong();

						completedExceptions++;

					}

				}

 

				double timeInHours = time / 3600000.0;

				double avgTime = completedExceptions == 0 ? 0 : timeInHours / completedExceptions;

 

				resolutionTime.put("resolutionTime", timeInHours);

				resolutionTime.put("avgResolutionTime", avgTime);

				resolutionTime.put("totalResolvedException", completedExceptions);

 

				resolutionTime.put("monthYear", String.format("%02d-%d", month + 1, year));

			} else {

 

				resolutionTime.put("resolutionTime", 0.0);

				resolutionTime.put("avgResolutionTime", 0.0);

				resolutionTime.put("totalResolvedException", 0L);

				resolutionTime.put("monthYear", String.format("%02d-%d", month + 1, year));

			}

		} catch (Exception e) {

 

		}

 

		return resolutionTime;

	}

 

	public ResponseEntity<String> postJsonToExternalApi(String externalApiUrl, String jsonBody) {

		try {

			HttpHeaders headers = new HttpHeaders();

			headers.setContentType(MediaType.APPLICATION_JSON);

			HttpEntity<String> requestEntity = new HttpEntity<>(jsonBody, headers);

			return restTemplate.postForEntity(externalApiUrl, requestEntity, String.class);

		} catch (Exception e) {

			throw new RuntimeException("Error while sending data to the external API: " + e.getMessage(), e);

		}

	}

 

	public String mapToJson(Map<String, String> map) {

		ObjectMapper objectMapper = new ObjectMapper();

		try {

			return objectMapper.writeValueAsString(map);

		} catch (JsonProcessingException e) {

			throw new RuntimeException("Error converting map to JSON", e);

		}

	}

 

	public String fetchTaskId(String processId) {

		String externalApiUrl = "http://" + ipAddress + ":8080/engine-rest/history/activity-instance?processInstanceId="

				+ processId + "&sortBy=startTime&sortOrder=desc";

		ResponseEntity<String> response = restTemplate.getForEntity(externalApiUrl, String.class);

		String responseBody = response.getBody();

		String taskId = null;

		if (responseBody != null) {

			ObjectMapper objectMapper = new ObjectMapper();

			try {

				JsonNode jsonArray = objectMapper.readTree(responseBody);

				if (jsonArray.isArray() && jsonArray.size() > 0) {

					JsonNode firstObject = jsonArray.get(0);

					JsonNode taskIdNode = firstObject.get("taskId");

					taskId = taskIdNode.asText();

				}

			} catch (Exception e) {

				throw new RuntimeException("Error parsing response to fetchTaskId: " + e.getMessage(), e);

			}

		}

		return taskId;

	}

 

	public String getProcessId(String exceptionId) {

		Optional<TemaExceptionEntity> exceptionOptional = temaExceptionRepository.findById(exceptionId);

 

		if (exceptionOptional.isPresent()) {

			TemaExceptionEntity exception = exceptionOptional.get();

			return exception.getProcessId();

		} else {

			return null;

		}

	}

 

	public JsonNode getUserGroups(String userId) throws Exception {

		String initialApiUrl = "http://" + ipAddress + ":8080/engine-rest/group?member=" + userId;

		JsonNode apiDataArray = restTemplate.getForObject(initialApiUrl, JsonNode.class);

 

		if (apiDataArray == null || !apiDataArray.isArray()) {

			throw new Exception("No groups found for user " + userId);

		}

		return apiDataArray;

	}
	
	public String fetchExceptionIdByProcessId(String processId) {

	    Optional<TemaExceptionEntity> exceptionEntityOptional = temaExceptionRepository.findExceptionByProcessId(processId);

 

	    if (exceptionEntityOptional.isPresent()) {

	        TemaExceptionEntity exceptionEntity = exceptionEntityOptional.get();

	        String exceptionId = exceptionEntity.getExceptionId();

	        return exceptionId;

	    }

	        return "";

	    

	}
 

 

}