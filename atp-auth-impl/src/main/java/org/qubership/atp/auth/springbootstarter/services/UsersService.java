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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import org.qubership.atp.auth.springbootstarter.Constants;
import org.qubership.atp.auth.springbootstarter.entities.ObjectPermissions;
import org.qubership.atp.auth.springbootstarter.entities.Operations;
import org.qubership.atp.auth.springbootstarter.entities.Permissions;
import org.qubership.atp.auth.springbootstarter.entities.Project;
import org.qubership.atp.auth.springbootstarter.entities.ServiceEntities;
import org.qubership.atp.auth.springbootstarter.entities.UserInfo;
import org.qubership.atp.auth.springbootstarter.services.client.UsersFeignClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@CacheConfig(cacheNames = {Constants.AUTH_PROJECTS_CACHE_NAME, Constants.AUTH_OBJECTS_CACHE_NAME})
public class UsersService {

    /**
     * Feign client to Users Service.
     */
    private final UsersFeignClient usersFeignClient;

    /**
     * Kafka Template.
     */
    private final KafkaTemplate<UUID, String> kafkaTemplate;

    /**
     * Topic name to send user service entities.
     */
    @Value("${kafka.service.entities.topic:service_entities}")
    private String topicName;

    /**
     * Service name.
     */
    @Value("${spring.application.name}")
    private String serviceName;

    /**
     * Return {@link Project} with user lists.
     * Stubbed implementation,
     *  so, instead of usersFeignClient.getUsersByProject(projectId),
     *  empty Project is returned.
     *
     * @param projectId UUID of a project
     * @return {@link Project}
     */
    @Cacheable(Constants.AUTH_PROJECTS_CACHE_NAME)
    public Project getUsersByProject(final UUID projectId) {
        Project project = new Project();
        project.setUuid(projectId);
        project.setLeads(new HashSet<>());
        project.setQaTaEngineers(new HashSet<>());
        project.setDevOpsEngineers(new HashSet<>());
        project.setAtpRunners(new HashSet<>());
        project.setAtpSupports(new HashSet<>());
        project.setPermissions(new Permissions());
        return project;
    }

    /**
     * Get User Permissions for project identified by id.
     *
     * @param projectId UUID of a project
     * @return User Permissions for the project.
     */
    public Permissions getPermissionsByProjectId(final UUID projectId) {
        Project project = getUsersByProject(projectId);
        return project.getPermissions();
    }

    /**
     * Sends service entities to the atp-users.
     * Stubbed implementation,
     *  so, instead of sending of Entities via kafkaTemplate.send() or usersFeignClient.save(),
     *  nothing is sent.
     * It's because no Kafka service and/or no Users-Service-Backend might be running.
     *
     * @param serviceEntities service entities to send.
     */
    public void sendEntities(final ServiceEntities serviceEntities) {
    }

    /**
     * Get object permissions for the entity inside the project.
     * Stubbed implementation, so, instead of
     *  usersFeignClient.getObjectPermissionsByObjectId(projectId, serviceName, getObjectName(entityName, objectId)),
     *  empty Map is returned.
     *
     * @param entityName String name of an entity
     * @param projectId UUID of a project
     * @param objectId UUID of an object
     * @return Map of permissions.
     */
    @Cacheable(Constants.AUTH_OBJECTS_CACHE_NAME)
    public Map<String, Map<UUID, Operations>> getPermissionsByObjectId(final String entityName,
                                                                       final UUID projectId,
                                                                       final UUID objectId) {
        return new HashMap<>();
    }

    /**
     * Get object permissions for the service inside the project.
     * Stubbed implementation, so, instead of
     *  usersFeignClient.getObjectPermissionsByServiceName(projectId, serviceName),
     *  empty Map is returned.
     *
     * @param projectId UUID of a project
     * @return Map of permissions.
     */
    public Map<String, Map<UUID, Operations>> getObjectPermissionsForService(final UUID projectId) {
        return new HashMap<>();
    }

    /**
     * Get UserInfo by Project ID and list of User IDs.
     * Stubbed implementation, so, instead of
     *  usersFeignClient.getUsersInfoByProjectId(projectId, userIds),
     *  List of stubbed UserInfo objects is returned.
     *
     * @param projectId UUID of a project
     * @param userIds List of user UUIDs
     * @return List of UserInfo objects.
     */
    public List<UserInfo> getUsersInfoByProjectId(final UUID projectId, final List<UUID> userIds) {
        List<UserInfo> userInfoList = new ArrayList<>();
        userIds.forEach(uuid -> {
            UserInfo userInfo = new UserInfo();
            userInfo.setId(uuid);
            userInfo.fillStubbedProperties();
            userInfoList.add(userInfo);
        });
        return userInfoList;
    }

    /**
     * Save users with permissions to object permissions.
     * Stubbed implementation, so, in fact, saving via usersFeignClient isn't performed.
     * This call isn't made:
     *  runWithoutUserToken(() -> usersFeignClient.saveObjectPermissions(projectId, serviceName,
     *                 getObjectName(entityName, objectId), assignedUsers)).
     * Instead, ObjectPermissions object is simply filled and returned.
     *
     * @param projectId UUID of a project
     * @param objectId UUID of an object
     * @param assignedUsers Map of user permissions
     * @return saved object permissions.
     */
    public ObjectPermissions saveObjectPermissions(final String entityName,
                                                   final UUID projectId,
                                                   final UUID objectId,
                                                   final Map<UUID, Operations> assignedUsers) {
        Map<String, Map<UUID, Operations>> permissions = new HashMap<>();
        permissions.put(objectId.toString(), assignedUsers);
        return new ObjectPermissions(UUID.randomUUID(), projectId, serviceName, permissions);
    }

    /**
     * Grants all rights to provided users for the object.
     *
     * @param projectId UUID of a project
     * @param objectId UUID of an object
     * @param assignedUsers List of user UUIDs
     * @return saved object permissions.
     */
    public ObjectPermissions grantAllPermissions(final String entityName,
                                                 final UUID projectId,
                                                 final UUID objectId,
                                                 final List<UUID> assignedUsers) throws Exception {
        Map<UUID, Operations> permissions = new HashMap<>();
        assignedUsers.forEach(userId ->
                permissions.put(userId, new Operations(true,true,true,true,true,true,true)));
        return saveObjectPermissions(entityName, projectId, objectId, permissions);
    }

    /**
     * Delete permissions for object by ID.
     * Stubbed implementation, so, in fact, deleting via usersFeignClient isn't performed.
     * This call isn't made:
     *   runWithoutUserToken(() -> usersFeignClient
     *          .deleteObjectPermissions(projectId, serviceName, getObjectName(entityName, objectId))).
     * Instead, method does nothing.
     *
     * @param entityName String entity name
     * @param projectId UUID of a project
     * @param objectId UUID of an object.
     */
    public void deleteObjectPermissions(final String entityName,
                                        final UUID projectId,
                                        final UUID objectId) {
    }

    /**
     * Delete all objects by object IDs.
     * Stubbed implementation, so, in fact, deleting via usersFeignClient isn't performed.
     * This call isn't made:
     *   runWithoutUserToken(() -> usersFeignClient
     *          .deleteObjectPermissionsBulk(projectId, serviceName, getObjectNames(entityName, objectIds))).
     * Instead, method does nothing.
     *
     * @param entityName String entity name
     * @param projectId UUID of a project
     * @param objectIds List of object UUIDs.
     */
    public void deleteObjectPermissionsBulk(final String entityName,
                                            final UUID projectId,
                                            final List<UUID> objectIds) {
    }

    /**
     * Execute a method without getting user token.
     *
     * @param callable Method to execute
     * @return result of execution
     * @throws Exception in case errors.
     */
    private <T> T runWithoutUserToken(Callable<T> callable) throws Exception {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        SecurityContextHolder.getContext().setAuthentication(null);
        try {
            return callable.call();
        } finally {
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
    }

    /**
     * Execute a method without getting user token.
     *
     * @param runnable Method to execute.
     */
    private void runWithoutUserToken(Runnable runnable) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        SecurityContextHolder.getContext().setAuthentication(null);
        try {
            runnable.run();
        } finally {
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
    }

    /**
     * Return name of entity in "serviceName-entityName-Id" format.
     *
     * @param entityName String entity name
     * @param objectId UUID of an object
     * @return String name calculated.
     */
    public String getObjectName(final String entityName,
                                final UUID objectId) {
        return String.format("%s-%s-%s", serviceName, entityName, objectId);
    }

    private List<String> getObjectNames(final String entityName,
                                        final List<UUID> objectId) {
        return objectId.stream()
                .map(id -> String.format("%s-%s-%s", serviceName, entityName, id))
                .collect(Collectors.toList());
    }

}
