/*
 * # Copyright 2024-2025 NetCracker Technology Corporation
 * #
 * # Licensed under the Apache License, Version 2.0 (the "License");
 * # you may not use this file except in compliance with the License.
 * # You may obtain a copy of the License at
 * #
 * #      http://www.apache.org/licenses/LICENSE-2.0
 * #
 * # Unless required by applicable law or agreed to in writing, software
 * # distributed under the License is distributed on an "AS IS" BASIS,
 * # WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * # See the License for the specific language governing permissions and
 * # limitations under the License.
 */

package org.qubership.atp.auth.springbootstarter.services;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.qubership.atp.auth.springbootstarter.entities.Group;
import org.qubership.atp.auth.springbootstarter.entities.Project;
import org.qubership.atp.auth.springbootstarter.entities.Role;
import org.qubership.atp.auth.springbootstarter.holders.DataContextHolder;

@RunWith(MockitoJUnitRunner.class)
public class GroupServiceTest {

    private UUID userId;
    private Project project;

    @Mock
    private UsersService usersService;
    @Mock
    private DataContextHolder<UUID> userIdContextHolder;
    @Mock
    private DataContextHolder<Set<String>> userRolesContextHolder;
    private UserGroupService userGroupService;

    @Before
    public void setUp() {
        userGroupService = new UserGroupService(userIdContextHolder, usersService, userRolesContextHolder);
    }

    private void fillAndCheckLeadsGroup(Project project, UUID userId, Set<String> userRoles) {
        Set<UUID> leads = new HashSet<>();
        leads.add(userId);
        project.setLeads(leads);

        Mockito.when(usersService.getUsersByProject(Mockito.any(UUID.class))).thenReturn(project);
        Mockito.when(userIdContextHolder.get()).thenReturn(Optional.of(userId));
        Mockito.when(userRolesContextHolder.get()).thenReturn(Optional.of(userRoles));

        assertEquals(Group.LEAD, userGroupService.getUserGroupByProjectId(UUID.randomUUID()));
    }

    @Test
    public void onGroupService_GetUserGroup_LeadGroup() {
        project = new Project();
        userId = UUID.randomUUID();

        fillAndCheckLeadsGroup(project, userId, Collections.emptySet());
    }

    @Test
    public void onGroupService_GetUserGroup_SupportRole_SupportGroup() {
        project = new Project();
        userId = UUID.randomUUID();
        Set<String> userRoles = new HashSet<>(Collections.singletonList( Role.ATP_SUPPORT.name()));

        Mockito.when(usersService.getUsersByProject(Mockito.any(UUID.class))).thenReturn(project);
        Mockito.when(userIdContextHolder.get()).thenReturn(Optional.of(userId));
        Mockito.when(userRolesContextHolder.get()).thenReturn(Optional.of(userRoles));

        assertEquals(Group.SUPPORT, userGroupService.getUserGroupByProjectId(UUID.randomUUID()));
    }

    @Test
    public void onGroupService_GetUserGroup_SupportAndLead_LeadGroup() {
        project = new Project();
        userId = UUID.randomUUID();
        Set<String> userRoles = new HashSet<>(Collections.singletonList( Role.ATP_SUPPORT.name()));

        fillAndCheckLeadsGroup(project, userId, userRoles);
    }

    @Test
    public void onGroupService_GetUserGroup_NoSupportRole_SupportGroup() {
        project = new Project();
        userId = UUID.randomUUID();

        Set<UUID> supports = new HashSet<>();
        supports.add(userId);
        project.setAtpSupports(supports);

        Mockito.when(usersService.getUsersByProject(Mockito.any(UUID.class))).thenReturn(project);
        Mockito.when(userIdContextHolder.get()).thenReturn(Optional.of(userId));
        Mockito.when(userRolesContextHolder.get()).thenReturn(Optional.of(Collections.emptySet()));

        assertEquals(Group.SUPPORT, userGroupService.getUserGroupByProjectId(UUID.randomUUID()));
    }
}
