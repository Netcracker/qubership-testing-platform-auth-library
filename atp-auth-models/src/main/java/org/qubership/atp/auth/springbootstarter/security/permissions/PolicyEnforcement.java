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
     * This method is used if the params is String.class instead of UUID.class and Action.class
     *
     * @return permission
     */
    default boolean checkAccess(String projectId, String action) {
        return checkAccess(StringUtils.isBlank(projectId) ? null : UUID.fromString(projectId),
                Operation.valueOf(action.toUpperCase()));
    }

    /**
     * This method is used if the params is UUID.class and String.class instead of Action.class
     *
     * @return permission
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
     * @return permission
     */
    boolean checkAccess(Set<UUID> projectIdSet, String action);

    /**
     * Performs evaluation of authorization policies using given current project and operation for
     * currently authenticated user.
     *
     * @return permission
     */
    boolean checkAccess(UUID projectId, Operation action);

    /**
     * Performs evaluation of authorization policies using given current project, entity name and operation for
     * currently authenticated user.
     * This method is used if the params is UUID.class and String.class instead of Action.class.
     *
     * @return permission
     */
    boolean checkAccess(String entityName, UUID projectId, Operation action);

    /**
     * Performs evaluation of authorization policies using given entity name, set of projects and operation for
     * currently authenticated user.
     *
     * @return permission
     */
    boolean checkAccess(String entityName, Set<UUID> projectIdSet, Operation action);

    /**
     * Performs evaluation of authorization policies using given entity name, set of projects and operation for
     * currently authenticated user.
     *
     * @return permission
     */
    boolean checkAccess(String entityName, Set<UUID> projectIdSet, String action);

    /**
     * Performs evaluation of authorization policies using given entity name, current project and operation for
     * currently authenticated user.
     *
     * @return permission
     */
    default boolean checkAccess(String entityName, UUID projectId, String action) {
        return checkAccess(entityName, projectId, Operation.valueOf(action.toUpperCase()));
    }

    /**
     * Performs evaluation of authorization policies using given entity name, current project and operation for
     * currently authenticated user.
     *
     * @return permission
     */
    default boolean checkAccess(String entityName, String projectId, String action) {
        return checkAccess(entityName, UUID.fromString(projectId), Operation.valueOf(action.toUpperCase()));
    }

    /**
     * Performs evaluation of authorization policies using given current project, objectId and operation for
     * currently authenticated user.
     *
     * @return permission
     */
    boolean checkAccess(String entityName, UUID projectId, UUID objectId, Operation operation);

    /**
     * Performs evaluation of authorization policies using given current project, objectId and operation for
     * currently authenticated user.
     *
     * @return permission
     */
    default boolean checkAccess(String entityName, UUID projectId, UUID objectId, String operation) {
        return checkAccess(entityName, projectId, objectId, Operation.valueOf(operation));
    }

    /**
     * Performs evaluation of authorization policies using given current project, set of objectIds and operation for
     * currently authenticated user.
     *
     * @return permission
     */
    boolean checkAccess(String entityName, UUID projectId, Set<UUID> objectIds, Operation operation);

    /**
     * Performs evaluation of authorization policies using given current project, set of objectIds and operation for
     * currently authenticated user.
     *
     * @return permission
     */
    default boolean checkAccess(String entityName, UUID projectId, Set<UUID> objectIds, String operation) {
        return checkAccess(entityName, projectId, objectIds, Operation.valueOf(operation));
    }

    /**
     * Performs evaluation of authorization policies using given current project, set of objectIds and operation for
     * currently authenticated user.
     *
     * @return permission
     */
    boolean checkExternalAccess(String entityName, UUID projectId, Operation action);

    default boolean checkExternalAccess(String projectId, String action) {
        return checkAccess(StringUtils.isBlank(projectId) ? null : UUID.fromString(projectId),
                Operation.valueOf(action.toUpperCase()));
    }

    /**
     * Performs evaluation of authorization policies using user role.
     */
    boolean isAdmin();

    boolean isExternal();

    /**
     * Performs evaluation of authorization policies using user role.
     */
    boolean isSupport();

    /**
     * Performs evaluation of authorization policies.
     */
    boolean isAuthenticated();

    /**
     * Create project entity with users fields.
     *
     * @param leads list of leads ID
     * @param qaTaEngineers list of QA/TA engineers ID
     * @param devOpsEngineers list of devops engineers ID
     * @param atpRunners list of atp runners ID
     * @return {@link Project}
     */
    default Project getProjectEntityWithGroup(UUID projectId, List<UUID> leads, List<UUID> qaTaEngineers,
                                              List<UUID> devOpsEngineers, List<UUID> atpRunners,
                                              List<UUID> atpSupports, Permissions permissions) {
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
     * @param project {@link Project}
     * @param operation {@link Operation}
     * @return result of checking policy
     */
    boolean checkPoliciesForOperation(Project project, Operation operation);

    boolean checkPoliciesForOperation(String entityName, Project project, Operation operation);
}
