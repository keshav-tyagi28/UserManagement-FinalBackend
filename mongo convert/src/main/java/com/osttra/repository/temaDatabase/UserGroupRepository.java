package com.osttra.repository.temaDatabase;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.osttra.entity.User;
import com.osttra.entity.UserGroup;

public interface UserGroupRepository extends MongoRepository<UserGroup, String> {

	
	@Query("{$or: [ {'groupId': {$regex: ?0, $options: 'i'}}, {'groupName': {$regex: ?0, $options: 'i'}}, {'description': {$regex: ?0, $options: 'i'}} ]}")
	Page<UserGroup> searchUserGroup(String search, Pageable pageable);
}
