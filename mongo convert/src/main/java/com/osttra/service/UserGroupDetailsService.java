package com.osttra.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import com.osttra.entity.User;
import com.osttra.entity.UserGroup;

public interface UserGroupDetailsService {

	
	public UserGroup saveUserGroup(UserGroup userGroup);
	
	public Page<UserGroup> search(String search, Pageable pageable);

	public List<UserGroup> getAllUserGroups();

	public UserGroup getUserGroupById(String userId);

	@Transactional
	public void deleteUserGroup(String userGroupId);
	
	Page<UserGroup> getAllUserGroupsWithPaging(int pageNumber, int pageSize);
}
