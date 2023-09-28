package com.osttra.repository.temaDatabase;


import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.osttra.entity.User;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
	
	@Query("{$or: [ {'username': {$regex: ?0, $options: 'i'}}, {'firstName': {$regex: ?0, $options: 'i'}}, {'lastName': {$regex: ?0, $options: 'i'}} ]}")
	Page<User> searchUsers(String search, Pageable pageable);

}
