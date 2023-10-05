package com.osttra.service;

import java.util.List;
import java.util.Map;

public interface HistoryService {

	

    List<String> getUserHistory(String username) throws Exception;

    List<String> getUserHistoryComplete(String username) throws Exception;

    Integer getUserGroupHistoryCompleted(String usergroupid) throws Exception;

    Integer getUserGroupHistoryAssigned(String usergroupid) throws Exception;

    Map<String, Integer> getEscalated() throws Exception;
    
    
}
