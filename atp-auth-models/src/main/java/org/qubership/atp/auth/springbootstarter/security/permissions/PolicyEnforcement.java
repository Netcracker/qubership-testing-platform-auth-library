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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.qubership.atp.auth.springbootstarter.entities.Operation;
import org.qubership.atp.auth.springbootstarter.entities.Permissions;
import org.qubership.atp.auth.springbootstarter.entities.Project;

/**
 * Check access entry point.
 */
public interface PolicyEnforcement {

    /**
     * This method is used if params are of String class instead of UUID and Action classes.
     *
     * @param projectId Project id
     * @param action Operation name (e.g. CREATE, READ, ...)
     * @return permission (true - allowed, false - forbidden).
     */
    default boolean checkAccess(String projectId, String action) {
        return checkAccess(StringUtils.isBlank(projectId) ? null : UUID.fromString(projectId),
                Operation.valueOf(action.toUpperCase()));
    }

    /**
     * This method is used if params are of UUID and String classes instead of Action class.
     *
     * @param projectId Project id
     * @param action Operation name (e.g. CREATE, READ, ...)
     * @return permission (true - allowed, false - forbidden).
     */
    default boolean checkAccess(UUID projectId, String action) {
        return checkAccess(projectId, Operation.valueOf(action.toUpperCase()));
    }


    /**
     * Performs evaluation of authorization policies using given set of projects and operation for
     * currently authenticated user, execute checkAccess (UUID projectId, String action) for each project in the set.
     * If for at least one project checkAccess(UUID projectId, String action) return false,
     * then the method will also return false.
     *
     * @param projectIdSet Set of Project ids
     * @param action Operation name (e.g. CREATE, READ, ...)
     * @return permission (true - allowed, false - forbidden).
     */
    boolean checkAccess(Set<UUID> projectIdSet, String action);

    /**
     * Performs evaluation of authorization policies using given current project and operation for
     * currently authenticated user.
     *
     * @param projectId Project id
     * @param action Operation (e.g. CREATE, READ, ...)
     * @return permission (true - allowed, false - forbidden).
     */
    boolean checkAccess(UUID projectId, Operation action);

    /**
     * Performs evaluation of authorization policies using given current project, entity name and operation for
     * currently authenticated user.
     * This method is used if params are of UUID class and String class instead of Action class.
     *
     * @param entityName Name of entity class
     * @param projectId Project id
     * @param action Operation (e.g. CREATE, READ, ...)
     * @return permission (true - allowed, false - forbidden).
     */
    boolean checkAccess(String entityName, UUID projectId, Operation action);

    /**
     * Performs evaluation of authorization policies using given entity name, set of projects and operation for
     * currently authenticated user.
     *
     * @param entityName Name of entity class
     * @param projectIdSet Set of Project ids
     * @param action Operation (e.g. CREATE, READ, ...)
     * @return permission (true - allowed, false - forbidden).
     */
    boolean checkAccess(String entityName, Set<UUID> projectIdSet, Operation action);

    /**
     * Performs evaluation of authorization policies using given entity name, set of projects and operation for
     * currently authenticated user.
     *
     * @param entityName Name of entity class
     * @param projectIdSet Set of Project ids
     * @param action Operation name (e.g. CREATE, READ, ...)
     * @return permission (true - allowed, false - forbidden).
     */
    boolean checkAccess(String entityName, Set<UUID> projectIdSet, String action);

    /**
     * Performs evaluation of authorization policies using given entity name, current project and operation for
     * currently authenticated user.
     *
     * @param entityName Name of entity class
     * @param projectId Project id
     * @param action Operation name (e.g. CREATE, READ, ...)
     * @return permission (true - allowed, false - forbidden).
     */
    default boolean checkAccess(String entityName, UUID projectId, String action) {
        return checkAccess(entityName, projectId, Operation.valueOf(action.toUpperCase()));
    }

    /**
     * Performs evaluation of authorization policies using given entity name, current project and operation for
     * currently authenticated user.
     *
     * @param entityName Name of entity class
     * @param projectId Project id
     * @param action Operation name (e.g. CREATE, READ, ...)
     * @return permission (true - allowed, false - forbidden).
     */
    default boolean checkAccess(String entityName, String projectId, String action) {
        return checkAccess(entityName, UUID.fromString(projectId), Operation.valueOf(action.toUpperCase()));
    }

    /**
     * Performs evaluation of authorization policies using given current project, objectId and operation for
     * currently authenticated user.
     *
     * @param entityName Name of entity class
     * @param projectId Project id
     * @param objectId Object id
     * @param operation Operation (e.g. CREATE, READ, ...)
     * @return permission (true - allowed, false - forbidden).
     */
    boolean checkAccess(String entityName, UUID projectId, UUID objectId, Operation operation);

    /**
     * Performs evaluation of authorization policies using given current project, objectId and operation for
     * currently authenticated user.
     *
     * @param entityName Name of entity class
     * @param projectId Project id
     * @param objectId Object id
     * @param operation Operation name (e.g. CREATE, READ, ...)
     * @return permission (true - allowed, false - forbidden).
     */
    default boolean checkAccess(String entityName, UUID projectId, UUID objectId, String operation) {
        return checkAccess(entityName, projectId, objectId, Operation.valueOf(operation));
    }

    /**
     * Performs evaluation of authorization policies using given current project, set of objectIds and operation for
     * currently authenticated user.
     *
     * @param entityName Name of entity class
     * @param projectId Project id
     * @param objectIds Set of  Object ids
     * @param operation Operation (e.g. CREATE, READ, ...)
     * @return permission (true - allowed, false - forbidden).
     */
    boolean checkAccess(String entityName, UUID projectId, Set<UUID> objectIds, Operation operation);

    /**
     * Performs evaluation of authorization policies using given current project, set of objectIds and operation for
     * currently authenticated user.
     *
     * @param entityName Name of entity class
     * @param projectId Project id
     * @param objectIds Set of  Object ids
     * @param operation Operation name (e.g. CREATE, READ, ...)
     * @return permission (true - allowed, false - forbidden).
     */
    default boolean checkAccess(String entityName,
                                UUID projectId,
                                Set<UUID> objectIds,
                                String operation) {
        return checkAccess(entityName, projectId, objectIds, Operation.valueOf(operation));
    }

    /**
     * Performs evaluation of authorization policies using given current project, set of objectIds and operation for
     * currently authenticated user.
     *
     * @param entityName Name of entity class
     * @param projectId Project id
     * @param action Operation (e.g. CREATE, READ, ...)
     * @return permission (true - allowed, false - forbidden).
     */
    boolean checkExternalAccess(String entityName, UUID projectId, Operation action);

    /**
     * Check external access permissions.
     *
     * @param projectId Project id
     * @param action Operation name (e.g. CREATE, READ, ...)
     * @return permission (true - allowed, false - forbidden).
     */
    default boolean checkExternalAccess(String projectId, String action) {
        return checkAccess(StringUtils.isBlank(projectId) ? null : UUID.fromString(projectId),
                Operation.valueOf(action.toUpperCase()));
    }

    /**
     * Performs evaluation of authorization policies using user role.
     *
     * @return true if the user has admin rights, otherwise false.
     */
    boolean isAdmin();

    /**
     * Check if it's external access.
     *
     * @return true/false.
     */
    boolean isExternal();

    /**
     * Performs evaluation of authorization policies using user role.
     *
     * @return true if the user has support rights, otherwise false.
     */
    boolean isSupport();

    /**
     * Performs evaluation of authorization policies.
     *
     * @return true if the user is authenticated, otherwise false.
     */
    boolean isAuthenticated();

    /**
     * Create project entity with users fields.
     *
     * @param projectId Project id
     * @param leads list of leads IDs
     * @param qaTaEngineers list of QA/TA engineers IDs
     * @param devOpsEngineers list of devops engineers IDs
     * @param atpRunners list of atp runners IDs
     * @param atpSupports list of atp supports IDs
     * @param permissions Permissions object
     * @return {@link Project} object.
     */
    default Project getProjectEntityWithGroup(UUID projectId,
                                              List<UUID> leads,
                                              List<UUID> qaTaEngineers,
                                              List<UUID> devOpsEngineers,
                                              List<UUID> atpRunners,
                                              List<UUID> atpSupports,
                                              Permissions permissions) {
        Project project = new Project();
        project.setUuid(projectId);
        project.setLeads(new HashSet<>(leads));
        project.setQaTaEngineers(new HashSet<>(qaTaEngineers));
        project.setDevOpsEngineers(new HashSet<>(devOpsEngineers));
        project.setAtpRunners(new HashSet<>(atpRunners));
        project.setAtpSupports(new HashSet<>(atpSupports));
        project.setPermissions(permissions);
        return project;
    }

    /**
     * Check policy for project.
     *
     * @param project {@link Project} object
     * @param operation {@link Operation} (e.g. CREATE, READ, ...)
     * @return result of checking policy
     */
    boolean checkPoliciesForOperation(Project project, Operation operation);

    /**
     * Check permissions to perform the operation against the entityName under the project.
     *
     * @param entityName Name of entity class
     * @param project Project object
     * @param operation Operation (e.g. CREATE, READ, ...)
     * @return permission (true - allowed, false - forbidden).
     */
    boolean checkPoliciesForOperation(String entityName, Project project, Operation operation);
}
