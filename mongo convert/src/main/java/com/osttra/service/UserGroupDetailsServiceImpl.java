package com.osttra.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.osttra.entity.UserGroup;
import com.osttra.repository.UserGroupRepository;

@Service
public class UserGroupDetailsServiceImpl implements UserGroupDetailsService {
	@Autowired
	UserGroupRepository userGroupRepository;

	@Override
	public UserGroup saveUserGroup(UserGroup userGroup) {
		return userGroupRepository.save(userGroup);
	}

	@Override
	public List<UserGroup> getAllUserGroups() {
		return userGroupRepository.findAll();
	}

	@Override
	public UserGroup getUserGroupById(String userId) {
		return userGroupRepository.findById(userId).orElse(null);
	}

	@Override
	@Transactional
	public void deleteUserGroup(String userGroupId) {
		userGroupRepository.deleteById(userGroupId);
	}
}
