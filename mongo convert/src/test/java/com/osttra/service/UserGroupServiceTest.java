package com.osttra.service;

import com.osttra.entity.UserGroup;
import com.osttra.repository.temaDatabase.UserGroupRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@SpringBootTest
public class UserGroupServiceTest {

    @Autowired
    private UserGroupDetailsServiceImpl userGroupDetailsService;

    @MockBean
    private UserGroupRepository userGroupRepository;

    @BeforeEach
    public void setUp() {
        // Define any necessary behavior for your mocks here
    }

    @Test
    public void testSaveUserGroup() {
        UserGroup userGroup = new UserGroup();
        userGroup.setGroupId("group1");
        userGroup.setGroupName("Group 1");

        when(userGroupRepository.save(userGroup)).thenReturn(userGroup);

        UserGroup savedUserGroup = userGroupDetailsService.saveUserGroup(userGroup);

        assertEquals(userGroup, savedUserGroup);
    }

    @Test
    public void testGetAllUserGroups() {
        List<UserGroup> userGroups = new ArrayList<>();
        when(userGroupRepository.findAll()).thenReturn(userGroups);

        List<UserGroup> result = userGroupDetailsService.getAllUserGroups();

        assertEquals(userGroups, result);
    }

    @Test
    public void testGetUserGroupById() {
        String groupId = "group1";
        UserGroup userGroup = new UserGroup();
        userGroup.setGroupId(groupId);

        when(userGroupRepository.findById(groupId)).thenReturn(Optional.of(userGroup));

        UserGroup result = userGroupDetailsService.getUserGroupById(groupId);

        assertEquals(userGroup, result);
    }

    @Test
    public void testDeleteUserGroup() {
        String groupId = "group1";

        userGroupDetailsService.deleteUserGroup(groupId);

        verify(userGroupRepository, times(1)).deleteById(groupId);
    }

    @Test
    public void testGetAllUserGroupsWithPaging() {
        int pageNumber = 1;
        int pageSize = 10;
        List<UserGroup> userGroups = new ArrayList<>();
        Page<UserGroup> userGroupPage = new PageImpl<>(userGroups);

        when(userGroupRepository.findAll(any(Pageable.class))).thenReturn(userGroupPage);

        Page<UserGroup> result = userGroupDetailsService.getAllUserGroupsWithPaging(pageNumber, pageSize);

        assertEquals(userGroupPage, result);
    }
}
