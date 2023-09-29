package com.osttra.service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.osttra.entity.User;
import com.osttra.repository.temaDatabase.UserRepository;

@Service
public class UserDetailServiceImpl implements UserDetailService{

	@Autowired
	UserRepository userRepository;
	
	 @Autowired
	    private RestTemplate restTemplate;

	 @Override
	 public ResponseEntity<String> sendJsonToExternalApi(String externalApiUrl, HttpMethod httpMethod, String jsonPayload) {
	        try {
	            // Create headers for the HTTP request
	            HttpHeaders headers = new HttpHeaders();
	            headers.setContentType(MediaType.APPLICATION_JSON);

	            // Create a request entity with the JSON object and headers
	            HttpEntity<String> requestEntity = new HttpEntity<>(jsonPayload, headers);

	            // Send the JSON data to the external API URL using the specified HTTP method
	            return restTemplate.exchange(
	                externalApiUrl,
	                httpMethod,
	                requestEntity,
	                String.class
	            );

	        } catch (RestClientException e) {
	            // Handle any exceptions specific to the REST request
	            throw new RuntimeException("Failed to send JSON to external API: " + e.getMessage());
	        }
	    }


	
	
	 @Override
	public User saveUser(User user) {
        return userRepository.save(user);
    }
	
	@Override
	public List<User> getAllUser() {
        return userRepository.findAll();
    }
	
	@Override
	public User getUserById(String username) {
        
        User user = userRepository.findById(username).orElse(null);
     
        return user;
    }
	
	
	@Override
	@Transactional
	public void deleteUser(String username)
	{
		User user= userRepository.findById(username).orElse(null);
			userRepository.delete(user);

	}

	
	@Override
	public Page<User> search(String search, Pageable pageable) {
        return userRepository.searchUsers(search, pageable);
    }




	@Override
	public Page<User> getAllUsers(int pageNumber, int pageSize) {
		Pageable pageable = PageRequest.of(pageNumber - 1, pageSize);
        return userRepository.findAll(pageable);
	}


}
