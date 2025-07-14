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

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.qubership.atp.auth.springbootstarter.entities.Group;
import org.qubership.atp.auth.springbootstarter.entities.Project;
import org.qubership.atp.auth.springbootstarter.holders.DataContextHolder;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UserGroupService {

    private final DataContextHolder<UUID> userIdContextHolder;
    private final UsersService usersService;
    private final DataContextHolder<Set<String>> userRolesContextHolder;

    /**
     * UserGroupService component.
     * @param userIdContextHolder - userIdContextHolder
     * @param usersService - usersProjectService
     * @param userRolesContextHolder - userRolesContextHolder
     */
    public UserGroupService(
            DataContextHolder<UUID> userIdContextHolder,
            UsersService usersService,
            DataContextHolder<Set<String>> userRolesContextHolder) {
        this.userIdContextHolder = userIdContextHolder;
        this.usersService = usersService;
        this.userRolesContextHolder = userRolesContextHolder;
    }

    /**
     * Return {@link Group} for currently authenticated user by project.
     *
     * @return {@link Group}
     */
    public Group getUserGroupByProjectId(UUID projectId) {
        Project project = usersService.getUsersByProject(projectId);
        log.debug("getUserGroupByProjectId: id = {}, project = {}", projectId, project);
        return getUserGroupByProject(project);
    }

    /**
     * Return {@link Group} for currently authenticated user by project.
     *
     * @return {@link Group}
     */
    public Group getUserGroupByProject(Project project) {
        Optional<UUID> userId = userIdContextHolder.get();

        Set<String> userRoles = userRolesContextHolder.get().orElse(null);

        if (userId.isPresent()) {
            return project.getUserGroup(userId.get(), userRoles);
        }
        return Group.DEFAULT;
    }

    /**
     * Return currently authenticated user.
     *
     * @return {@link Optional}
     */
    public Optional<UUID> getUserId() {
        return userIdContextHolder.get();
    }
}
