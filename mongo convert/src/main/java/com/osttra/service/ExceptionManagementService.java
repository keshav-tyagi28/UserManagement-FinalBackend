package com.osttra.service;

 

	import com.osttra.entity.SourceExceptionEntity;
	import com.osttra.entity.TemaExceptionEntity;
	import org.springframework.http.ResponseEntity;

 

	import java.util.List;
	import java.util.Map;

 

	public interface ExceptionManagementService {

 

		List<SourceExceptionEntity> getAllFromSource();

 
		public String fetchExceptionIdByProcessId(String processId);
		
	    SourceExceptionEntity addExceptionInSource(SourceExceptionEntity sourceData);

 

	    void deleteAllItemsSource();

 

	    void deleteAllItemsTema();

 

	    void migrateData();

 

	    String fetchProcessId(String exceptionId, String exceptionType);

 

	    String extractProcessIdFromResponse(String responseBody);

 

	    TemaExceptionEntity getExceptionDetails(String exceptionId);

 

	    List<TemaExceptionEntity> getAllTemaException();

 

	    String assignExceptionToGroup(Map<String, String> assignGroup);

 

	    ResponseEntity<String> deleteOtherUser(String taskId);

 

	    ResponseEntity<String> updateCandidateGroup(String taskId, String groupId);

 

	    List<TemaExceptionEntity> getUserAssignedExceptions(String userId);

 

	    List<TemaExceptionEntity> getUserAssignedFourEyeCheckUpExceptions(String userId) throws Exception;

 

	    List<TemaExceptionEntity> getUnclaimedPerformTaskExceptions(String userId) throws Exception;

	    List<TemaExceptionEntity> getUnclaimedFourEyeExceptions(String userId);

 

	    ResponseEntity<String> claimExceptionByUser(Map<String, String> userExceptionIdMap);

 

	    ResponseEntity<String> completeFourEyeCheckUpTask(String exceptionId) throws Exception;

 

	    List<Map<String, Object>> getExceptionHistory(String exceptionId);

 

	    String completeTask(Map<String, String> request, int value);

 

	    void updateResolutionCount(String exceptionId, int resolutionCount);

 

//	    Map<String, Object> getEscalationCount(String start, String end);
//
//	    Map<String, Object> getresolutionTime(String start, String end);

 

	    ResponseEntity<String> postJsonToExternalApi(String externalApiUrl, String jsonBody);

 

	    String mapToJson(Map<String, String> map);

 

	    String fetchTaskId(String processId);

 

	    String getProcessId(String exceptionId);

 

}