package com.osttra.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class HistoryServiceImpl implements HistoryService {

	
	@Autowired
	ExceptionManagementService exceptionManagementService;
	
	private String ip = "10.196.22.55:8080";
	
	 public static JsonNode fetchExternalApiData(String externalApiUrl) throws Exception {
	        RestTemplate restTemplate = new RestTemplate();
	        String jsonResponse = restTemplate.getForObject(externalApiUrl, String.class);
	        ObjectMapper objectMapper = new ObjectMapper();
	        return objectMapper.readTree(jsonResponse);
	    }
	
	
	public List<String> getUserHistory(String username) throws Exception
	{
		String externalApiUrl = "http://" + ip + "/engine-rest/history/user-operation?userId=" + username
				+ "&operationType=Claim";
		
		JsonNode jsonNode= fetchExternalApiData(externalApiUrl);

		List<String> exceptionIds = new ArrayList<String>();
		for (JsonNode node : jsonNode) {
			String rootProcessInstanceId = node.get("rootProcessInstanceId").asText();
			String id = exceptionManagementService.fetchExceptionIdByProcessId(rootProcessInstanceId);
			if (id != "") {
				exceptionIds.add(id);
			}		
	}
		
		return exceptionIds;
	}
	
	
	
	
	
	
	
	public List<String> getUserHistoryComplete(String username) throws Exception
	{
		
		
			String externalApiUrl = "http://" + ip + "/engine-rest/history/user-operation?userId=" + username
				+ "&operationType=Complete";
			
			JsonNode jsonNode= fetchExternalApiData(externalApiUrl);
			
			List<String> exceptionIds = new ArrayList<String>();
			for (JsonNode node : jsonNode) {
				String rootProcessInstanceId = node.get("rootProcessInstanceId").asText();
				String id = exceptionManagementService.fetchExceptionIdByProcessId(rootProcessInstanceId);
				if (id != "") {
					exceptionIds.add(id);
				}
			}
			
			return exceptionIds;
			
	}
	
	
	public Integer getUserGroupHistoryCompleted(String usergroupid) throws Exception
	{
		String externalApiUrl = "http://" + ip + "/engine-rest/history/task/count?finished=true&taskHadCandidateGroup=" + usergroupid;

		
		JsonNode jsonNode= fetchExternalApiData(externalApiUrl);
			

		    JsonNode countNode = jsonNode.get("count");
		    int count= countNode.intValue();
		    
		    return count;

	}
	
	
	
	
	public Integer getUserGroupHistoryAssigned(String usergroupid) throws Exception
	{
		String externalApiUrl = "http://" + ip + "/engine-rest/history/task/count?&taskHadCandidateGroup=" + usergroupid;
		
		JsonNode jsonNode= fetchExternalApiData(externalApiUrl);
		
	    JsonNode countNode = jsonNode.get("count");
	    int count= countNode.intValue();
	    return count;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	public Map<String, Integer> getEscalated() throws JsonMappingException, JsonProcessingException
	{
		
		Map<String , Integer> m= new HashMap<String, Integer>();
		String externalApiUrl = "http://" + ip + "/engine-rest/history/task?&taskHadCandidateGroup=EscalationGroup" ;
		
		RestTemplate restTemplate = new RestTemplate();

		String jsonResponse = restTemplate.getForObject(externalApiUrl, String.class);

		ObjectMapper objectMapper = new ObjectMapper();

		JsonNode jsonNode = objectMapper.readTree(jsonResponse);
		
		List<String> processid = new ArrayList<String>();
		for (JsonNode node : jsonNode) {
			String rootProcessInstanceId = node.get("rootProcessInstanceId").asText();
			processid.add(rootProcessInstanceId);			
		}

	
	for(String pid: processid)
	{
		String externalApiUrl2 = "http://" + ip + "/engine-rest/history/activity-instance?processInstanceId="+pid+"&activityName=Perform Task" ;
		
		String jsonResponse2 = restTemplate.getForObject(externalApiUrl2, String.class);
		JsonNode jsonNode2 = objectMapper.readTree(jsonResponse2);
		
		for (JsonNode node : jsonNode2) {
			String taskid = node.get("taskId").asText();


			String externalApiUrl3 = "http://" + ip + "/engine-rest/history/identity-link-log?sortBy=time&sortOrder=desc&taskId="+taskid+
					"&type=candidate&operationType=add";
			
			String jsonResponse3 = restTemplate.getForObject(externalApiUrl3, String.class);
			JsonNode jsonNode3 = objectMapper.readTree(jsonResponse3);
			
				JsonNode first= jsonNode3.get(0);
			
				String groupid = first.get("groupId").asText();
				
				if (m.containsKey(groupid)) {
			        m.put(groupid, m.get(groupid) + 1);
			    } else {
			        m.put(groupid, 1);
			    }				
			
		}
		
	}
	
	
	
	return m;
	
	}
	
	}
