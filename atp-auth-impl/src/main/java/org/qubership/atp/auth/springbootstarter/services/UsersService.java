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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@CacheConfig(cacheNames = {"projects", "auth_objects"})
public class UsersService {

    private final UsersFeignClient usersFeignClient;
    private final KafkaTemplate<UUID, String> kafkaTemplate;
    @Value("${kafka.service.entities.topic:service_entities}")
    private String topicName;
    @Value("${spring.application.name}")
    private String serviceName;

    /**
     * Return {@link Project} with user lists.
     *
     * @return {@link Project}
     */
    @Cacheable("projects")
    public Project getUsersByProject(UUID projectId) {
        return usersFeignClient.getUsersByProject(projectId);
    }

    public Permissions getPermissionsByProjectId(UUID projectId) {
        Project project = getUsersByProject(projectId);
        return project.getPermissions();
    }

    /**
     * Sends service entities to the atp-users service via rest or via kafka, according to the kafka.enable property.
     *
     * @param serviceEntities service entities to send
     */
    public void sendEntities(ServiceEntities serviceEntities) throws JsonProcessingException {
        if (kafkaTemplate != null) {
            ObjectMapper mapper = new ObjectMapper();
            kafkaTemplate.send(topicName, mapper.writeValueAsString(serviceEntities));
        } else {
            usersFeignClient.save(serviceEntities);
        }
    }

    @Cacheable("auth_objects")
    public Map<String, Map<UUID, Operations>> getPermissionsByObjectId(String entityName, UUID projectId,
                                                                       UUID objectId) {
        return usersFeignClient.getObjectPermissionsByObjectId(projectId, serviceName,
                getObjectName(entityName, objectId));
    }

    public Map<String, Map<UUID, Operations>> getObjectPermissionsForService(UUID projectId) {
        return usersFeignClient.getObjectPermissionsByServiceName(projectId, serviceName);
    }

    public List<UserInfo> getUsersInfoByProjectId(UUID projectId, List<UUID> userIds) {
        return usersFeignClient.getUsersInfoByProjectId(projectId, userIds);
    }

    /**
     * Save users with permissions to object permissions.
     *
     * @param projectId projectId
     * @param objectId objectId
     * @param assignedUsers assignedUsers with permissions
     * @return saved object
     */
    public ObjectPermissions saveObjectPermissions(String entityName, UUID projectId,
                                                   UUID objectId, Map<UUID, Operations> assignedUsers)
            throws Exception {
        return runWithoutUserToken(() -> usersFeignClient.saveObjectPermissions(projectId, serviceName,
                getObjectName(entityName, objectId), assignedUsers));
    }

    /**
     * Grants all rights to provided users in the object.
     *
     * @param projectId projectId
     * @param objectId objectId
     * @param assignedUsers assignedUsers
     * @return saved object
     */
    public ObjectPermissions grantAllPermissions(String entityName, UUID projectId,
                                                 UUID objectId, List<UUID> assignedUsers) throws Exception {
        Map<UUID, Operations> permissions = new HashMap<>();
        assignedUsers.forEach(userId ->
                permissions.put(userId, new Operations(true,true,true,true,true,true,true)));
        return saveObjectPermissions(entityName, projectId, objectId, permissions);
    }


    /**
     * delete permisions for object with id.
     * @param entityName entity name
     * @param projectId project id
     * @param objectId object id
     */
    public void deleteObjectPermissions(String entityName, UUID projectId, UUID objectId) {
        runWithoutUserToken(() ->
                usersFeignClient.deleteObjectPermissions(projectId, serviceName, getObjectName(entityName, objectId)));
    }

    /**
     * delete all objects with object ids.
     * @param entityName entity name
     * @param projectId project id
     * @param objectIds object ids
     */
    public void deleteObjectPermissionsBulk(String entityName, UUID projectId, List<UUID> objectIds) {
        runWithoutUserToken(() ->
                usersFeignClient.deleteObjectPermissionsBulk(projectId, serviceName,
                        getObjectNames(entityName, objectIds)));
    }

    /**
     * execute method without getting user token.
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
     * execute method without getting user token.
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
     * return name for entity in next format serviceName-entityName-Id.
     */
    public String getObjectName(String entityName, UUID objectId) {
        return String.format("%s-%s-%s", serviceName, entityName, objectId);
    }

    private List<String> getObjectNames(String entityName, List<UUID> objectId) {
        return objectId.stream()
                .map(id -> String.format("%s-%s-%s", serviceName, entityName, id))
                .collect(Collectors.toList());
    }
}
