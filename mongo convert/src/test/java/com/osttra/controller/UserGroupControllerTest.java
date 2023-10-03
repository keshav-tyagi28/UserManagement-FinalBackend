package com.osttra.controller;

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

import com.osttra.entity.User;
import com.osttra.entity.UserGroup;
import com.osttra.repository.temaDatabase.UserGroupRepository;
import com.osttra.repository.temaDatabase.UserRepository;
import com.osttra.service.UserDetailService;
import com.osttra.service.UserGroupDetailsService;
import com.osttra.to.CustomResponse;
import com.osttra.to.CustomResponseWithTotalRecords;

import javax.servlet.http.HttpServletRequest;

import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class UserGroupControllerTest {

    @InjectMocks
    private UserGroupController userGroupController;

    @Mock
    private UserGroupDetailsService userGroupDetailsService;

    @Mock
    private UserDetailService userDetailService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserGroupRepository userGroupRepository;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testAddUserGroup() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getServletPath()).thenReturn("/usergroups/registerusergroup");

        UserGroup existingUserGroup = new UserGroup();
        existingUserGroup.setGroupId("group1");

        when(userGroupDetailsService.getUserGroupById("group1")).thenReturn(existingUserGroup);

        UserGroup userGroup = new UserGroup();
        userGroup.setGroupId("group2");
        userGroup.setGroupName("Test Group");

        ResponseEntity<?> response = userGroupController.addUserGroup(userGroup, request);

        verify(userGroupDetailsService, times(1)).getUserGroupById("group2");
        verify(userGroupDetailsService, times(1)).saveUserGroup(any(UserGroup.class));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("User group added successfully", ((CustomResponse<?>) response.getBody()).getMessage());
    }
    
    @Test
    public void testGetAllUserGroups() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getServletPath()).thenReturn("/usergroups/allgroups");

        Page<UserGroup> userGroupPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 5), 10);

        when(userGroupDetailsService.getAllUserGroupsWithPaging(1, 1)).thenReturn(userGroupPage);

        ResponseEntity<?> response = userGroupController.getAllUserGroups(1, request, 1);

        verify(userGroupDetailsService, times(1)).getAllUserGroupsWithPaging(1, 1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Listed all user groups", ((CustomResponseWithTotalRecords<?>) response.getBody()).getMessage());
    }
   

}
