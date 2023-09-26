package com.osttra.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.osttra.entity.UserGroup;
import com.osttra.repository.UserGroupRepository;


@Service
public class UserGroupDetailsService {
	@Autowired
	UserGroupRepository  userGroupRepository;

	public UserGroup saveUserGroup(UserGroup userGroup) {
		return userGroupRepository.save(userGroup);
	}
	
	public List<UserGroup> getAllUserGroups() {
        return userGroupRepository.findAll();
    }
	
	public UserGroup getUserGroupById(String userId) {
        return userGroupRepository.findById(userId).orElse(null);
    }
	
	@Transactional
	public void deleteUserGroup(String userGroupId)
	{
		userGroupRepository.deleteById(userGroupId);
	}
}
