package com.osttra.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.osttra.entity.User;
import com.osttra.entity.UserGroup;
import com.osttra.repository.temaDatabase.UserGroupRepository;

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

	
	@Override
	public Page<UserGroup> search(String search, Pageable pageable) {
        return userGroupRepository.searchUserGroup(search, pageable);
    }

	
	
	
	@Override
	public Page<UserGroup> getAllUserGroupsWithPaging(int pageNumber, int pageSize) {
        Pageable paging = PageRequest.of(pageNumber - 1, pageSize);
        return userGroupRepository.findAll(paging);
    }
}
