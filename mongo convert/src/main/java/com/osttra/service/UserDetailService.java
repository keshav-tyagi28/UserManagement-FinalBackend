package com.osttra.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

import com.osttra.entity.User;

public interface UserDetailService {

	public ResponseEntity<String> sendJsonToExternalApi(String externalApiUrl, HttpMethod httpMethod, String jsonPayload);

	 

	public User saveUser(User user);

	public List<User> getAllUser();

	public User getUserById(String username);

	@Transactional
	public void deleteUser(String username);

	public Page<User> search(String search, Pageable pageable);

	public Page<User> getAllUsers(int pageNumber,int pageSize);

}
