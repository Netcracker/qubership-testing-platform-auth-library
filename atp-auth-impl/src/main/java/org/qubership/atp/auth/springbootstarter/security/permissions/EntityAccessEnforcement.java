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

package org.qubership.atp.auth.springbootstarter.security.permissions;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.qubership.atp.auth.springbootstarter.entities.Group;
import org.qubership.atp.auth.springbootstarter.entities.Operation;
import org.qubership.atp.auth.springbootstarter.entities.Operations;
import org.qubership.atp.auth.springbootstarter.entities.Project;
import org.qubership.atp.auth.springbootstarter.entities.Role;
import org.qubership.atp.auth.springbootstarter.holders.DataContextHolder;
import org.qubership.atp.auth.springbootstarter.services.UserGroupService;
import org.qubership.atp.auth.springbootstarter.services.UsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EntityAccessEnforcement implements PolicyEnforcement {

    private final UsersService usersService;
    private final UserGroupService userGroupService;
    private final DataContextHolder<Set<String>> userRolesContextHolder;
    private final String serviceName;

    private static final String PROJECT_PERMISSIONS = "atp-catalogue-Project";
    private static final String DEFAULT_PERMISSIONS = "DEFAULT";

    /**
     * Constructor.
     *
     * @param usersService           users service
     * @param userGroupService       user group service
     * @param userRolesContextHolder user roles context holder
     */
    @Autowired
    public EntityAccessEnforcement(UsersService usersService,
                                   UserGroupService userGroupService,
                                   DataContextHolder<Set<String>> userRolesContextHolder,
                                   String serviceName) {
        this.usersService = usersService;
        this.userGroupService = userGroupService;
        this.userRolesContextHolder = userRolesContextHolder;
        this.serviceName = serviceName;
    }

    @Override
    public boolean checkAccess(Set<UUID> projectIdSet, String action) {
        boolean isAccess = false;

        if (isAdmin()) {
            return true;
        }

        if (projectIdSet == null) {
            return false;
        }

        for (UUID projectId : projectIdSet) {
            isAccess = checkAccess(projectId, action);
            if (!isAccess) {
                break;
            }
        }

        return isAccess;
    }

    @Override
    public boolean checkAccess(UUID projectId, Operation operation) {
        if (isAdmin()) {
            return true;
        }
        if (projectId == null) {
            return false;
        }

        Group group = userGroupService.getUserGroupByProjectId(projectId);
        return checkPolicies(operation, group, projectId, "");
    }

    @Override
    public boolean checkAccess(String entityName, UUID projectId, Operation operation) {
        if (isAdmin()) {
            return true;
        }
        if (projectId == null) {
            return false;
        }
        Group group = userGroupService.getUserGroupByProjectId(projectId);
        return checkPolicies(operation, group, projectId, entityName);
    }

    @Override
    public boolean checkAccess(String entityName, Set<UUID> projectIdSet, Operation action) {
        boolean isAccess = false;

        if (isAdmin()) {
            return true;
        }

        if (projectIdSet == null) {
            return false;
        }

        for (UUID projectId : projectIdSet) {
            isAccess = checkAccess(entityName, projectId, action);
            if (!isAccess) {
                break;
            }
        }

        return isAccess;
    }

    @Override
    public boolean checkAccess(String entityName, Set<UUID> projectIdSet, String action) {
        boolean isAccess = false;

        if (isAdmin()) {
            return true;
        }

        if (projectIdSet == null) {
            return false;
        }

        for (UUID projectId : projectIdSet) {
            isAccess = checkAccess(entityName, projectId, action);
            if (!isAccess) {
                break;
            }
        }

        return isAccess;
    }

    @Override
    public boolean checkAccess(String entityName, UUID projectId, UUID objectId, Operation operation) {
        if (isAdmin()) {
            return true;
        }

        if (!checkAccess(entityName, projectId, operation)) {
            log.debug("User has no rights to the project - access denied");
            return false;
        }

        if (Objects.isNull(objectId)) {
            log.debug("ObjectId is null. Checking access to the object is not required - access is granted");
            return true;
        }

        Optional<UUID> userIdOpt = userGroupService.getUserId();
        if (!userIdOpt.isPresent()) {
            log.debug("User is not defined - access denied");
            return false;
        }
        UUID userId = userIdOpt.get();

        Map<String, Map<UUID, Operations>> permissions =
                usersService.getPermissionsByObjectId(entityName, projectId, objectId);
        String objectName = usersService.getObjectName(entityName, objectId);
        Map<UUID, Operations> assignedUsers = permissions.get(objectName);
        if (Objects.isNull(assignedUsers)) {
            log.debug("Object with name: {} and id {} not found - access denied", entityName, objectId);
            return false;
        }
        Operations ops = assignedUsers.get(userId);
        if (Objects.isNull(ops)) {
            log.debug("User with id {} not assigned to object (entity: {}, id: {}) - access denied", userId,
                    entityName, objectId);
            return false;
        }
        return ops.isOperationAvailable(operation);
    }

    @Override
    public boolean checkAccess(String entityName, UUID projectId, Set<UUID> objectIds, Operation operation) {
        if (isAdmin()) {
            return true;
        }

        if (!checkAccess(entityName, projectId, operation)) {
            log.debug("User has no rights to the project - access denied");
            return false;
        }

        if (CollectionUtils.isEmpty(objectIds)) {
            log.debug("ObjectIds are empty or null. Checking access to the object is not required - access is granted");
            return true;
        }

        Optional<UUID> userIdOpt = userGroupService.getUserId();
        if (!userIdOpt.isPresent()) {
            log.debug("User is not defined - access denied");
            return false;
        }
        UUID userId = userIdOpt.get();

        Map<String, Map<UUID, Operations>> permissions =
                usersService.getObjectPermissionsForService(projectId);
        return objectIds.stream().allMatch(objectId -> {
            String objectName = usersService.getObjectName(entityName, objectId);
            Map<UUID, Operations> assignedUsers = permissions.get(objectName);
            if (Objects.isNull(assignedUsers)) {
                log.debug("Object with name: {} and id {} not found - access denied", entityName, objectId);
                return false;
            }
            Operations ops = assignedUsers.get(userId);
            if (Objects.isNull(ops)) {
                log.debug("User with id {} not assigned to object (entity: {}, id: {}) - access denied", userId,
                        entityName, objectId);
                return false;
            }
            return ops.isOperationAvailable(operation);
        });
    }

    /**
     * Performs evaluation of authorization policies using user role.
     */
    public boolean isAdmin() {
        return hasRole(Role.ATP_ADMIN);
    }

    /**
     * Performs evaluation of authorization policies using user role.
     */
    public boolean isSupport() {
        return hasRole(Role.ATP_SUPPORT);
    }

    private boolean hasRole(Role role) {
        Optional<Set<String>> userRoles = userRolesContextHolder.get();

        if (userRoles.isPresent() && !userRoles.get().isEmpty()) {
            return userRoles.get()
                    .stream()
                    .anyMatch(userRole -> role.name().equalsIgnoreCase(userRole));
        }

        return false;
    }

    /**
     * Performs evaluation of authorization policies.
     */
    @Override
    public boolean isAuthenticated() {
        return userGroupService.getUserId().isPresent();
    }

    /**
     * Check access to project.
     *
     * @param project   current project
     * @param operation for check access to project
     * @return result access to current project for user
     */
    private boolean checkAccessForProject(String entityName, Project project, Operation operation) {
        if (isAdmin()) {
            return true;
        }

        Group group = userGroupService.getUserGroupByProject(project);
        return checkPolicies(operation, group, project, entityName);
    }

    private boolean checkPolicies(Operation operation, Group group, Project project, String entityName) {
        if (project.getPermissions() == null) {
            return checkPolicies(operation, group, project.getUuid(), entityName);
        }
        Map<String, Operations> permissionsInGroup = project.getPermissions().getPermissionsByGroup(group);
        return checkPolicies(operation, permissionsInGroup, entityName);
    }

    private boolean checkPolicies(Operation operation, Group group, UUID projectId, String entityName) {
        Map<String, Operations> permissionsInGroup = usersService.getPermissionsByProjectId(projectId)
                .getPermissionsByGroup(group);
        return checkPolicies(operation, permissionsInGroup, entityName);
    }

    private boolean checkPolicies(Operation operation, Map<String, Operations> permissionsInGroup, String entityName) {
        if (permissionsInGroup == null) {
            return false;
        }
        if (entityName.isEmpty()) {
            return checkPoliciesForOperation(permissionsInGroup, operation);
        }
        return checkPoliciesForOperationInEntity(permissionsInGroup, entityName, operation);
    }

    /**
     * Check access for operation.
     *
     * @param permissions permissions for user group
     * @param operation   {@link Operation}
     * @return access for operation
     */
    private boolean checkPoliciesForOperation(Map<String, Operations> permissions, Operation operation) {
        Operations operations = permissions.get(PROJECT_PERMISSIONS);
        if (operations == null) {
            operations = permissions.get(DEFAULT_PERMISSIONS);
            if (operations == null) {
                return false;
            }
        }
        return operations.isOperationAvailable(operation);
    }

    @Override
    public boolean checkPoliciesForOperation(Project project, Operation operation) {
        return checkAccessForProject("", project, operation);
    }

    @Override
    public boolean checkPoliciesForOperation(String entityName, Project project, Operation operation) {
        return checkAccessForProject(entityName, project, operation);
    }

    private boolean checkPoliciesForOperationInEntity(Map<String, Operations> permissions, String entityName,
                                                      Operation operation) {
        Operations operations = permissions.get(serviceName + "-" + entityName);
        if (operations == null) {
            return checkPoliciesForOperation(permissions, operation);
        }
        return operations.isOperationAvailable(operation);
    }
}
