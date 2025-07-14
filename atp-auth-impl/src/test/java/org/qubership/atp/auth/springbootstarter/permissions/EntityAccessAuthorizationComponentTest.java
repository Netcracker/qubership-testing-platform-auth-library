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

package org.qubership.atp.auth.springbootstarter.permissions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.qubership.atp.auth.springbootstarter.entities.Role.ATP_ADMIN;
import static org.qubership.atp.auth.springbootstarter.entities.Role.ATP_SUPPORT;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.qubership.atp.auth.springbootstarter.entities.Group;
import org.qubership.atp.auth.springbootstarter.entities.Operation;
import org.qubership.atp.auth.springbootstarter.entities.Operations;
import org.qubership.atp.auth.springbootstarter.entities.Permissions;
import org.qubership.atp.auth.springbootstarter.entities.Project;
import org.qubership.atp.auth.springbootstarter.entities.UserInfo;
import org.qubership.atp.auth.springbootstarter.holders.DataContextHolder;
import org.qubership.atp.auth.springbootstarter.security.permissions.EntityAccessEnforcement;
import org.qubership.atp.auth.springbootstarter.services.UserGroupService;
import org.qubership.atp.auth.springbootstarter.services.UsersService;
import org.qubership.atp.auth.springbootstarter.services.client.UsersFeignClient;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class EntityAccessAuthorizationComponentTest {

    @Mock
    private UsersService usersService;
    @Mock
    private UserGroupService userGroupService;
    @Mock
    private DataContextHolder<Set<String>> userRolesContextHolder;
    @Mock
    private UsersFeignClient usersFeignClient;
    @InjectMocks
    private EntityAccessEnforcement authorizationComponent;

    @Before
    public void init() {
        Permissions permissions = new Permissions();
        permissions.setQaTaEngineers(new HashMap<String, Operations>() {{
            put("DEFAULT", new Operations(false, true, false, false, true, true, false));
            put("service-name-test", new Operations(true, true, false, false, true, true, false));
        }});

        ReflectionTestUtils.setField(authorizationComponent, "serviceName", "service-name");
        when(usersService.getPermissionsByProjectId(any(UUID.class))).thenReturn(permissions);
        when(userGroupService.getUserGroupByProjectId(any())).thenReturn(Group.ENGINEER);
    }

    @Test
    public void onEntityAccessEnforcement_CheckAccess_GetPermission() {
        assertTrue(authorizationComponent.checkAccess(UUID.randomUUID(), Operation.EXECUTE));
    }

    @Test
    public void onEntityAccessEnforcement_CheckAccess_NotGetPermission() {
        assertFalse(authorizationComponent.checkAccess(UUID.randomUUID(), Operation.CREATE));
    }

    @Test
    public void onEntityAccessEnforcement_CheckAccess_AdminGetPermission() {
        HashSet<String> userRoles = new HashSet<>();
        userRoles.add(ATP_ADMIN.name());

        when(userRolesContextHolder.get()).thenReturn(Optional.of(userRoles));

        assertTrue(authorizationComponent.checkAccess(UUID.randomUUID(), Operation.CREATE));
    }

    @Test
    public void onEntityAccessEnforcement_IsAuthenticated() {
        when(userGroupService.getUserId())
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(UUID.randomUUID()));
        assertFalse(authorizationComponent.isAuthenticated());
        assertTrue(authorizationComponent.isAuthenticated());
    }

    @Test
    public void onEntityAccessEnforcement_IsSupport() {
        HashSet<String> userRoles = new HashSet<>();
        userRoles.add(ATP_SUPPORT.name());

        when(userRolesContextHolder.get()).thenReturn(Optional.of(userRoles));

        assertTrue(authorizationComponent.isSupport());
    }

    @Test
    public void onEntityAccessEnforcement_IsAdmin() {
        HashSet<String> userRoles = new HashSet<>();
        userRoles.add(ATP_ADMIN.name());

        when(userRolesContextHolder.get()).thenReturn(Optional.of(userRoles));

        assertTrue(authorizationComponent.isAdmin());
    }

    @Test
    public void onEntityAccessEnforcement_CheckAccess_WithEntity_IsAdmin() {
        HashSet<String> userRoles = new HashSet<>();
        userRoles.add(ATP_ADMIN.name());
        when(userRolesContextHolder.get()).thenReturn(Optional.of(userRoles));
        assertTrue(authorizationComponent.checkAccess("test", UUID.randomUUID(), Operation.DELETE));
    }

    @Test
    public void onEntityAccessEnforcement_CheckAccess_WithEntity_ProjectIdIsNull() {
        assertFalse(authorizationComponent.checkAccess("test", (UUID) null, Operation.DELETE));
    }

    @Test
    public void onEntityAccessEnforcement_CheckAccess_WithEntity() {
        UUID projectId = UUID.randomUUID();
        assertTrue(authorizationComponent.checkAccess("test", projectId, Operation.CREATE));
        assertTrue(authorizationComponent.checkAccess("test", projectId, Operation.READ));
        assertFalse(authorizationComponent.checkAccess("test", projectId, Operation.UPDATE));
        assertFalse(authorizationComponent.checkAccess("test", projectId, Operation.DELETE));
        assertTrue(authorizationComponent.checkAccess("test", projectId, Operation.EXECUTE));
    }

    @Test
    public void onEntityAccessEnforcement_CheckAccess_GetPermissionWithEntityName_EntityNotExist() {
        assertFalse(authorizationComponent.checkAccess("test1", UUID.randomUUID(), Operation.CREATE));
    }

    @Test
    public void onEntityAccessEnforcement_CheckAccess_GetPermissionWithEntityName_AndSetOfProjects_WithEntity() {
        UUID projectId = UUID.randomUUID();
        assertTrue(authorizationComponent.checkAccess("test", new HashSet<>(Collections.singletonList(projectId)), Operation.READ));
    }

    private void checkOperationPolicies(Project project, Operation operation, boolean[] expectedPermissions) {
        for (boolean expectedPermission : expectedPermissions) {
            if (expectedPermission) {
                assertTrue(authorizationComponent.checkPoliciesForOperation(project, operation));
            } else {
                assertFalse(authorizationComponent.checkPoliciesForOperation(project, operation));
            }
        }
    }

    @Test
    public void onEntityAccessEnforcement_CheckPoliciesForOperation_withoutServiceEntity() {
        Project project = new Project();
        project.setUuid(UUID.randomUUID());
        UUID userId = UUID.randomUUID();
        project.setLeads(Collections.singleton(userId));
        Permissions permissions = new Permissions();
        permissions.setLeads(new HashMap<String, Operations>() {{
            put("DEFAULT", new Operations(true, true, true, true, true, true, true));
        }});
        permissions.setQaTaEngineers(new HashMap<String, Operations>() {{
            put("DEFAULT", new Operations(true, true, true, false, true, true, false));
        }});
        permissions.setDevOpsEngineers(new HashMap<String, Operations>() {{
            put("DEFAULT", new Operations(false, true, false, false, true, false, false));
        }});
        permissions.setAtpRunners(new HashMap<String, Operations>() {{
            put("DEFAULT", new Operations(false, true, false, false, true, false, false));
        }});
        permissions.setAtpSupports(new HashMap<String, Operations>() {{
            put("DEFAULT", new Operations(false, true, false, false, true, false, false));
        }});
        project.setPermissions(permissions);

        when(userGroupService.getUserGroupByProject(project))
                // check CREATE permissions for all roles
                .thenReturn(Group.LEAD, Group.ENGINEER, Group.DEVOPS, Group.EXECUTOR, Group.SUPPORT)
                // check READ permissions for all roles
                .thenReturn(Group.LEAD, Group.ENGINEER, Group.DEVOPS, Group.EXECUTOR, Group.SUPPORT)
                // check UPDATE permissions for all roles
                .thenReturn(Group.LEAD, Group.ENGINEER, Group.DEVOPS, Group.EXECUTOR, Group.SUPPORT)
                // check DELETE permissions for all roles
                .thenReturn(Group.LEAD, Group.ENGINEER, Group.DEVOPS, Group.EXECUTOR, Group.SUPPORT)
                // check EXECUTE permissions for all roles
                .thenReturn(Group.LEAD, Group.ENGINEER, Group.DEVOPS, Group.EXECUTOR, Group.SUPPORT)
                // check LOCK permissions for all roles
                .thenReturn(Group.LEAD, Group.ENGINEER, Group.DEVOPS, Group.EXECUTOR, Group.SUPPORT)
                // check UNLOCK permissions for all roles
                .thenReturn(Group.LEAD, Group.ENGINEER, Group.DEVOPS, Group.EXECUTOR, Group.SUPPORT);

        checkOperationPolicies(project, Operation.CREATE, new boolean[]{true, true, false, false, false});

        checkOperationPolicies(project, Operation.READ, new boolean[]{true, true, true, true, true});

        checkOperationPolicies(project, Operation.UPDATE, new boolean[]{true, true, false, false, false});

        checkOperationPolicies(project, Operation.DELETE, new boolean[]{true, false, false, false, false});

        checkOperationPolicies(project, Operation.EXECUTE, new boolean[]{true, true, true, true, true});

        checkOperationPolicies(project, Operation.LOCK, new boolean[]{true, true, false, false, false});

        checkOperationPolicies(project, Operation.UNLOCK, new boolean[]{true, false, false, false, false});
    }

    private void checkEntityOperationPolicies(String entityName,
                                              Project project,
                                              Operation operation,
                                              boolean[] expectedPermissions) {
        for (boolean expectedPermission : expectedPermissions) {
            if (expectedPermission) {
                assertTrue(authorizationComponent.checkPoliciesForOperation(entityName, project, operation));
            } else {
                assertFalse(authorizationComponent.checkPoliciesForOperation(entityName, project, operation));
            }
        }
    }

    @Test
    public void onEntityAccessEnforcement_CheckPoliciesForOperation_withServiceEntity() {
        Project project = new Project();
        project.setUuid(UUID.randomUUID());
        UUID userId = UUID.randomUUID();
        project.setLeads(Collections.singleton(userId));
        Permissions permissions = new Permissions();
        permissions.setLeads(new HashMap<String, Operations>() {{
            put("DEFAULT", new Operations(true, true, true, true, true, true, true));
            put("service-name-test", new Operations(false, false, false, false, false, false, false));
        }});
        permissions.setQaTaEngineers(new HashMap<String, Operations>() {{
            put("DEFAULT", new Operations(true, true, true, false, true, true, false));
            put("service-name-test", new Operations(false, false, false, true, false, false, true));
        }});
        permissions.setDevOpsEngineers(new HashMap<String, Operations>() {{
            put("DEFAULT", new Operations(false, true, false, false, true, false, false));
            put("service-name-test", new Operations(true, false, true, true, false, true, true));
        }});
        permissions.setAtpRunners(new HashMap<String, Operations>() {{
            put("DEFAULT", new Operations(false, true, false, false, true, false, false));
            put("service-name-test", new Operations(true, false, true, true, false, true, true));
        }});
        permissions.setAtpSupports(new HashMap<String, Operations>() {{
            put("DEFAULT", new Operations(false, true, false, false, true, false, false));
            put("service-name-test", new Operations(true, false, true, true, false, true, true));
        }});
        project.setPermissions(permissions);

        when(userGroupService.getUserGroupByProject(project))
                .thenReturn(Group.LEAD, Group.ENGINEER, Group.DEVOPS, Group.EXECUTOR, Group.SUPPORT)
                .thenReturn(Group.LEAD, Group.ENGINEER, Group.DEVOPS, Group.EXECUTOR, Group.SUPPORT)
                .thenReturn(Group.LEAD, Group.ENGINEER, Group.DEVOPS, Group.EXECUTOR, Group.SUPPORT)
                .thenReturn(Group.LEAD, Group.ENGINEER, Group.DEVOPS, Group.EXECUTOR, Group.SUPPORT)
                .thenReturn(Group.LEAD, Group.ENGINEER, Group.DEVOPS, Group.EXECUTOR, Group.SUPPORT)
                .thenReturn(Group.LEAD, Group.ENGINEER, Group.DEVOPS, Group.EXECUTOR, Group.SUPPORT)
                .thenReturn(Group.LEAD, Group.ENGINEER, Group.DEVOPS, Group.EXECUTOR, Group.SUPPORT);

        // check that custom permission override default values
        checkEntityOperationPolicies("test", project, Operation.CREATE,
                new boolean[]{false, false, true, true, true});

        checkEntityOperationPolicies("test", project, Operation.READ,
                new boolean[]{false, false, false, false, false});

        checkEntityOperationPolicies("test", project, Operation.UPDATE,
                new boolean[]{false, false, true, true, true});

        checkEntityOperationPolicies("test", project, Operation.DELETE,
                new boolean[]{false, true, true, true, true});

        checkEntityOperationPolicies("test", project, Operation.EXECUTE,
                new boolean[]{false, false, false, false, false});

        checkEntityOperationPolicies("test", project, Operation.LOCK,
                new boolean[]{false, false, true, true, true});

        checkEntityOperationPolicies("test", project, Operation.UNLOCK,
                new boolean[]{false, true, true, true, true});
    }

    @Test
    public void onEntityAccessEnforcement_CheckPoliciesForOperation_withServiceEntity_ButCustomPermissionNotExist() {
        Project project = new Project();
        project.setUuid(UUID.randomUUID());
        UUID userId = UUID.randomUUID();
        project.setLeads(Collections.singleton(userId));
        Permissions permissions = new Permissions();
        permissions.setLeads(new HashMap<String, Operations>() {{
            put("DEFAULT", new Operations(true, true, true, true, true, true, true));
        }});
        permissions.setQaTaEngineers(new HashMap<String, Operations>() {{
            put("DEFAULT", new Operations(true, true, true, false, true, true, false));
        }});
        permissions.setDevOpsEngineers(new HashMap<String, Operations>() {{
            put("DEFAULT", new Operations(false, true, false, false, true, false, false));
        }});
        permissions.setAtpRunners(new HashMap<String, Operations>() {{
            put("DEFAULT", new Operations(false, true, false, false, true, false, false));
        }});
        permissions.setAtpSupports(new HashMap<String, Operations>() {{
            put("DEFAULT", new Operations(false, true, false, false, true, false, false));
        }});
        project.setPermissions(permissions);

        when(userGroupService.getUserGroupByProject(project))
                .thenReturn(Group.LEAD, Group.ENGINEER, Group.DEVOPS, Group.EXECUTOR, Group.SUPPORT)
                .thenReturn(Group.LEAD, Group.ENGINEER, Group.DEVOPS, Group.EXECUTOR, Group.SUPPORT)
                .thenReturn(Group.LEAD, Group.ENGINEER, Group.DEVOPS, Group.EXECUTOR, Group.SUPPORT)
                .thenReturn(Group.LEAD, Group.ENGINEER, Group.DEVOPS, Group.EXECUTOR, Group.SUPPORT)
                .thenReturn(Group.LEAD, Group.ENGINEER, Group.DEVOPS, Group.EXECUTOR, Group.SUPPORT)
                .thenReturn(Group.LEAD, Group.ENGINEER, Group.DEVOPS, Group.EXECUTOR, Group.SUPPORT)
                .thenReturn(Group.LEAD, Group.ENGINEER, Group.DEVOPS, Group.EXECUTOR, Group.SUPPORT);

        checkEntityOperationPolicies("test", project, Operation.CREATE,
                new boolean[]{true, true, false, false, false});

        checkEntityOperationPolicies("test", project, Operation.READ,
                new boolean[]{true, true, true, true, true});

        checkEntityOperationPolicies("test", project, Operation.UPDATE,
                new boolean[]{true, true, false, false, false});

        checkEntityOperationPolicies("test", project, Operation.DELETE,
                new boolean[]{true, false, false, false, false});

        checkEntityOperationPolicies("test", project, Operation.EXECUTE,
                new boolean[]{true, true, true, true, true});

        checkEntityOperationPolicies("test", project, Operation.LOCK,
                new boolean[]{true, true, false, false, false});

        checkEntityOperationPolicies("test", project, Operation.UNLOCK,
                new boolean[]{true, false, false, false, false});
    }

    @Test
    public void onEntityAccessEnforcement_CheckPoliciesForOperation_withoutServiceEntity_ProjectPermissionsSpecified() {
        Project project = new Project();
        project.setUuid(UUID.randomUUID());
        UUID userId = UUID.randomUUID();
        project.setLeads(Collections.singleton(userId));
        Permissions permissions = new Permissions();
        permissions.setLeads(new HashMap<String, Operations>() {{
            put("DEFAULT", new Operations(true, true, true, true, true, true, true));
            put("atp-catalogue-Project", new Operations(false, true, true, true, true, true, true));
        }});
        project.setPermissions(permissions);

        when(userGroupService.getUserGroupByProject(project))
                // check CREATE permissions for all roles
                .thenReturn(Group.LEAD);

        // catalogue permissions must have higher a priority than the default
        assertFalse(authorizationComponent.checkPoliciesForOperation(project, Operation.CREATE));
    }

    @Test
    public void onEntityAccessEnforcement_checkAccess_withObjectId() {
        // given
        UUID projectId = UUID.randomUUID();
        UUID objectId = UUID.randomUUID();
        UUID userId1 = UUID.randomUUID();
        UUID userId2 = UUID.randomUUID();
        Operation op = Operation.CREATE;
        String entityName = "entity";
        String objectName = "entity" + objectId;

        Permissions permissions = new Permissions();
        permissions.setLeads(new HashMap<String, Operations>() {{
            put("service-name-entity", new Operations(true, true, true, true, true, true, true));
        }});
        permissions.setAtpRunners(new HashMap<String, Operations>() {{
            put("service-name-entity", new Operations(false, true, false, false, true, false, false));
        }});
        Map<String, Map<UUID, Operations>> objectPermissions = new HashMap<String, Map<UUID, Operations>>(){{
            put(objectName, new HashMap<UUID, Operations>(){{
                put(userId1, new Operations(true, true, true, true, true, true, true));
            }});
        }};

        // when
        when(userGroupService.getUserGroupByProjectId(any(UUID.class)))
                // check CREATE permissions for all roles
                .thenReturn(Group.LEAD)
                .thenReturn(Group.EXECUTOR);
        when(usersService.getPermissionsByProjectId(any(UUID.class)))
                .thenReturn(permissions)
                .thenReturn(permissions);
        when(usersService.getPermissionsByObjectId(entityName, projectId, objectId))
                .thenReturn(objectPermissions)
                .thenReturn(objectPermissions);
        when(userGroupService.getUserId())
                .thenReturn(Optional.of(userId1))
                .thenReturn(Optional.of(userId2));
        when(usersService.getObjectName(entityName, objectId))
                .thenReturn(objectName)
                .thenReturn(objectName);

        // then
        // user 1 exists in object permissions
        assertTrue(authorizationComponent.checkAccess(entityName, projectId, objectId, op));
        // user 2 not exists in object permissions
        assertFalse(authorizationComponent.checkAccess(entityName, projectId, objectId, op));
    }

    @Test
    public void onEntityAccessEnforcement_checkAccess_withObjectIds() {
        // given
        UUID projectId = UUID.randomUUID();
        UUID objectId1 = UUID.randomUUID();
        UUID objectId2 = UUID.randomUUID();
        UUID userId1 = UUID.randomUUID();
        UUID userId2 = UUID.randomUUID();
        UUID userId3 = UUID.randomUUID();
        Operation op = Operation.CREATE;
        String entityName = "entity";
        String objectName1 = "entity" + objectId1;
        String objectName2 = "entity" + objectId2;

        Permissions permissions = new Permissions();
        permissions.setLeads(new HashMap<String, Operations>() {{
            put("service-name-entity", new Operations(true, true, true, true, true, true, true));
        }});
        permissions.setAtpRunners(new HashMap<String, Operations>() {{
            put("service-name-entity", new Operations(false, true, false, false, true, false, false));
        }});
        Map<String, Map<UUID, Operations>> projectPermissions = new HashMap<String, Map<UUID, Operations>>(){{
            put(objectName1, new HashMap<UUID, Operations>(){{
                put(userId1, new Operations(true, true, true, true, true, true, true));
                put(userId3, new Operations(true, true, true, true, true, true, true));
            }});
            put(objectName2, new HashMap<UUID, Operations>(){{
                put(userId2, new Operations(true, true, true, true, true, true, true));
                put(userId3, new Operations(true, true, true, true, true, true, true));
            }});
        }};

        // when
        when(userGroupService.getUserGroupByProjectId(any(UUID.class)))
                .thenReturn(Group.LEAD);
        when(usersService.getPermissionsByProjectId(any(UUID.class)))
                .thenReturn(permissions);
        when(usersService.getObjectPermissionsForService(projectId))
                .thenReturn(projectPermissions);
        when(userGroupService.getUserId())
                .thenReturn(Optional.of(userId1))
                .thenReturn(Optional.of(userId2))
                .thenReturn(Optional.of(userId3));
        when(usersService.getObjectName(any(), any()))
                .thenReturn(objectName1)
                .thenReturn(objectName2)
                .thenReturn(objectName1)
                .thenReturn(objectName2)
                .thenReturn(objectName1)
                .thenReturn(objectName2);

        // then
        // user 1 exists in object permissions
        assertFalse(authorizationComponent.checkAccess(entityName, projectId,
                new HashSet<>(Arrays.asList(objectId1, objectId2)), op));
        // user 2 not exists in object permissions
        assertFalse(authorizationComponent.checkAccess(entityName, projectId,
                new HashSet<>(Arrays.asList(objectId1, objectId2)), op));
        // user 3 not exists in object permissions
        assertTrue(authorizationComponent.checkAccess(entityName, projectId,
                new HashSet<>(Arrays.asList(objectId1, objectId2)), op));
    }

    @Test
    public void getUsersInfoByProjectId() {
        // given
        UUID projectId = UUID.randomUUID();
        UserInfo user1 = new UserInfo();
        user1.setId(UUID.randomUUID());
        user1.setUsername("Name");
        user1.setLastName("LastName");
        user1.setEmail("email");
        user1.addRole(Group.LEAD.name());
        user1.addRole(Group.DEVOPS.name());
        UserInfo user2 = new UserInfo();
        user2.setUsername("Name2");
        user2.setLastName("LastName2");
        user2.addRole(Group.EXECUTOR.name());
        List<UserInfo> userInfoList = Arrays.asList(user1, user2);
        List<UUID> userList = Arrays.asList(user1.getId(), user2.getId());
        UsersService usersService = new UsersService(usersFeignClient, null);

        // when
        when(usersFeignClient.getUsersInfoByProjectId(eq(projectId), eq(userList))).thenReturn(userInfoList);

        // then
        assertEquals(userInfoList, usersService.getUsersInfoByProjectId(projectId, userList));
    }
}
