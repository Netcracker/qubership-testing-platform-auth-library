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

package org.qubership.atp.auth.springbootstarter.entities;

import java.io.Serializable;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Project implements Serializable {

    private static final long serialVersionUID = -7878008971600457198L;

    /**
     * Project identifier.
     */
    private UUID uuid;

    /**
     * Set of user uuids having QA/TA Lead role for the Project.
     */
    private Set<UUID> leads;

    /**
     * Set of user uuids having QA/TA Engineer role for the Project.
     */
    private Set<UUID> qaTaEngineers;

    /**
     * Set of user uuids having DevOps Engineer role for the Project.
     */
    private Set<UUID> devOpsEngineers;

    /**
     * Set of user uuids having Runner role for the Project.
     */
    private Set<UUID> atpRunners;

    /**
     * Set of user uuids having Support role.
     */
    private Set<UUID> atpSupports;

    /**
     * Roles permissions to perform operations against objects.
     */
    private Permissions permissions;

    /**
     * Return {@link Group} for currently authenticated user by project.
     *
     * @param userId to check groups
     * @param userRoles Set of user role names
     * @return {@link Group} the most privileged Group the User belongs to.
     */
    public Group getUserGroup(final UUID userId, final Set<String> userRoles) {
        if (!Objects.isNull(leads) && leads.contains(userId)) {
            return Group.LEAD;
        }

        if (!Objects.isNull(qaTaEngineers) && qaTaEngineers.contains(userId)) {
            return Group.ENGINEER;
        }

        if (!Objects.isNull(devOpsEngineers) && devOpsEngineers.contains(userId)) {
            return Group.DEVOPS;
        }

        if (!Objects.isNull(atpRunners) && atpRunners.contains(userId)) {
            return Group.EXECUTOR;
        }

        if (isSupportGroup(userRoles) || isSupportGroupByUserId(userId)) {
            return Group.SUPPORT;
        }

        return Group.DEFAULT;
    }

    private boolean isSupportGroupByUserId(final UUID userId) {
        return !Objects.isNull(atpSupports) && atpSupports.contains(userId);
    }

    private boolean isSupportGroup(final Set<String> userRoles) {
        return !CollectionUtils.isEmpty(userRoles) && userRoles
                .stream()
                .anyMatch(role -> Role.ATP_SUPPORT.name().equalsIgnoreCase(role));
    }
}
