package com.osttra.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.client.RestTemplate;

import com.osttra.entity.User;
import com.osttra.repository.temaDatabase.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
public class UserServiceTest {

    @Autowired
    private UserDetailService userDetailService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private RestTemplate restTemplate;

    @Test
    public void testSaveUser() {
        User user = new User(); // Create a user instance

        when(userRepository.save(user)).thenReturn(user); // Mock the UserRepository behavior

        User savedUser = userDetailService.saveUser(user);

        assertEquals(user, savedUser);
    }

    @Test
    public void testGetAllUser() {
        List<User> userList = new ArrayList<>(); // Create a list of users
        when(userRepository.findAll()).thenReturn(userList); // Mock the UserRepository behavior

        List<User> result = userDetailService.getAllUser();

        assertEquals(userList, result);
    }

    @Test
    public void testGetUserById() {
        String username = "testUser";
        User user = new User(); // Create a user instance
        when(userRepository.findById(username)).thenReturn(Optional.of(user)); // Mock the UserRepository behavior

        User result = userDetailService.getUserById(username);

        assertEquals(user, result);
    }

    @Test
    public void testDeleteUser() {
        String username = "testUser";
        User user = new User(); // Create a user instance
        when(userRepository.findById(username)).thenReturn(Optional.of(user)); // Mock the UserRepository behavior

        userDetailService.deleteUser(username);

        verify(userRepository).delete(user);
    }

    @Test
    public void testSearch() {
        String search = "example";
        Pageable pageable = PageRequest.of(0, 10);

        // Mock the UserRepository behavior for search
        List<User> userList = new ArrayList<>();
        Page<User> userPage = new PageImpl<>(userList, pageable, userList.size());
        when(userRepository.searchUsers(search, pageable)).thenReturn(userPage);

        Page<User> result = userDetailService.search(search, pageable);

        assertEquals(userPage, result);
    }

    @Test
    public void testGetAllUsers() {
        int pageNumber = 1;
        int pageSize = 10;
        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize);

        // Mock the UserRepository behavior for findAll
        List<User> userList = new ArrayList<>();
        Page<User> userPage = new PageImpl<>(userList, pageable, userList.size());
        when(userRepository.findAll(pageable)).thenReturn(userPage);

        Page<User> result = userDetailService.getAllUsers(pageNumber, pageSize);

        assertEquals(userPage, result);
    }
}
