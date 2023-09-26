package com.osttra.repository;


import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.osttra.entity.User;

@Repository
public interface UserRepository extends MongoRepository<User, String> {

}
