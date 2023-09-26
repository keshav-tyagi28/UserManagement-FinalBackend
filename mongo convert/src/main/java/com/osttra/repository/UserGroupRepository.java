package com.osttra.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.osttra.entity.UserGroup;

public interface UserGroupRepository extends MongoRepository<UserGroup, String> {

}
