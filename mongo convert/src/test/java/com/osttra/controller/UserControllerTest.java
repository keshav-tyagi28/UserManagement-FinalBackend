package com.osttra.controller;

import com.osttra.entity.User;
import com.osttra.entity.UserGroup;
import com.osttra.repository.temaDatabase.UserRepository;
import com.osttra.service.UserDetailService;
import com.osttra.service.UserGroupDetailsService;
import com.osttra.to.CustomResponse;
import com.osttra.to.CustomResponseWithTotalRecords;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

public class UserControllerTest {

    @InjectMocks
    private UserController userController;

    @Mock
    private UserDetailService userDetailService;
    
    @Mock
    private UserGroupDetailsService userGroupDetailsService;
    
    @Mock
    private UserGroupControllerTest userGroupController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }
    

    @Test
    public void testGetAllUsers() {
        // Mock request parameters
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getServletPath()).thenReturn("/users/allusers");

        // Mock the behavior of userDetailService.getAllUsers
        Page<User> mockPage = mock(Page.class);
        when(mockPage.getContent()).thenReturn(Collections.singletonList(new User())); // Mock user data
        when(mockPage.getTotalElements()).thenReturn(1L);
        when(userDetailService.getAllUsers(anyInt(), anyInt())).thenReturn(mockPage);

        // Call the method to test
        ResponseEntity<?> response = userController.getAllUsers(1, request, 5);

        // Verify that userDetailService.getAllUsers was called
        verify(userDetailService, times(1)).getAllUsers(1, 5);

        // Assert the response status and content
        assert response.getStatusCode() == HttpStatus.OK;
        assert response.getBody() instanceof CustomResponseWithTotalRecords;
        CustomResponseWithTotalRecords<?> responseBody = (CustomResponseWithTotalRecords<?>) response.getBody();
        assert responseBody.getMessage().equals("Listed all users");
        assert responseBody.getTotalRecords() == 1;
    }
    
    
    @Test
    public void testUpdateUser() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getServletPath()).thenReturn("/users/update/testuser");

        User existingUser = new User();
        existingUser.setUsername("testuser");

        when(userDetailService.getUserById("testuser")).thenReturn(existingUser);

        User updatedUser = new User();
        updatedUser.setFirstName("UpdatedFirstName");
        updatedUser.setLastName("UpdatedLastName");
        updatedUser.setEmail("updated@example.com");

        ResponseEntity<?> response = userController.updateUser("testuser", updatedUser, request);

        verify(userDetailService, times(1)).getUserById("testuser");
        verify(userDetailService, times(1)).saveUser(any(User.class));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Updated Successfully", ((CustomResponse<?>) response.getBody()).getMessage());
    }
    
    @Test
    public void testGetUserGroups() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getServletPath()).thenReturn("/users/testuser/groups");

        User user = new User();
        user.setUsername("testuser");
        user.setUserGroupsId(Collections.singleton("group1"));

        when(userDetailService.getUserById("testuser")).thenReturn(user);
        when(userGroupDetailsService.getUserGroupById("group1")).thenReturn(new UserGroup());

        ResponseEntity<?> response = userController.getUserGroups("testuser", request);

        verify(userDetailService, times(1)).getUserById("testuser");
        verify(userGroupDetailsService, times(1)).getUserGroupById("group1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("User groups displayed succesfully", ((CustomResponse<?>) response.getBody()).getMessage());
    }
    
    @Test
    public void testGetSpecificUser() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getServletPath()).thenReturn("/users/finduser/testuser");

        User user = new User();
        user.setUsername("testuser");

        when(userDetailService.getUserById("testuser")).thenReturn(user);

        ResponseEntity<?> response = userController.getSpecificUser("testuser", request);

        verify(userDetailService, times(1)).getUserById("testuser");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("User found", ((CustomResponse<?>) response.getBody()).getMessage());
    }
    
    
    @Test
    public void testGetResource() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getServletPath()).thenReturn("/users/search/resource");

        Page<User> userPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 5), 10);

        when(userDetailService.search("search", PageRequest.of(0, 5))).thenReturn(userPage);

        ResponseEntity<?> response = userController.getResource("search", 1, request);

        verify(userDetailService, times(1)).search("search", PageRequest.of(0, 5));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Listed all searched users", ((CustomResponseWithTotalRecords<?>) response.getBody()).getMessage());
    }


    
    
}
