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

package org.qubership.atp.auth.springbootstarter.config;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.qubership.atp.auth.springbootstarter.entities.Operation;
import org.qubership.atp.auth.springbootstarter.entities.Permissions;
import org.qubership.atp.auth.springbootstarter.entities.Project;
import org.qubership.atp.auth.springbootstarter.entities.UserInfo;
import org.qubership.atp.auth.springbootstarter.provider.impl.DisableSecurityUserProvider;
import org.qubership.atp.auth.springbootstarter.security.permissions.PolicyEnforcement;
import org.qubership.atp.auth.springbootstarter.ssl.Provider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@Profile(value = {"default","disable-security"})
public class DisableSecurityConfiguration extends WebSecurityConfigurerAdapter {

    /**
     * Allow all PolicyEnforcement, will be used where you do not need to check permissions.
     */
    @Bean("entityAccess")
    public PolicyEnforcement entityAccessEnforcement() {
        return new PolicyEnforcement() {
            @Override
            public boolean checkAccess(String projectId, String action) {
                return true;
            }

            @Override
            public boolean checkAccess(UUID projectId, String action) {
                return true;
            }

            @Override
            public boolean checkAccess(Set<UUID> projectIdSet, String action) {
                return true;
            }

            @Override
            public boolean checkAccess(UUID projectId, Operation action) {
                return true;
            }

            @Override
            public boolean checkAccess(String entityName, UUID projectId, Operation action) {
                return true;
            }

            @Override
            public boolean checkAccess(String entityName, Set<UUID> projectIdSet, Operation action) {
                return true;
            }

            @Override
            public boolean checkAccess(String entityName, Set<UUID> projectIdSet, String action) {
                return true;
            }

            @Override
            public boolean checkAccess(String entityName, UUID projectId, String action) {
                return true;
            }

            @Override
            public boolean checkAccess(String entityName, String projectId, String action) {
                return true;
            }

            @Override
            public boolean checkAccess(String entityName, UUID projectId, UUID objectId, Operation operation) {
                return true;
            }

            @Override
            public boolean checkAccess(String entityName, UUID projectId, UUID objectId, String operation) {
                return true;
            }

            @Override
            public boolean checkAccess(String entityName, UUID projectId, Set<UUID> objectIds, Operation operation) {
                return true;
            }

            @Override
            public boolean checkAccess(String entityName, UUID projectId, Set<UUID> objectIds, String operation) {
                return true;
            }

            @Override
            public boolean checkExternalAccess(String entityName, UUID projectId, Operation action) {
                return true;
            }

            @Override
            public boolean checkExternalAccess(String projectId, String action) {
                return true;
            }

            @Override
            public boolean isAdmin() {
                return true;
            }

            @Override
            public boolean isExternal() {
                return true;
            }

            @Override
            public boolean isSupport() {
                return true;
            }

            @Override
            public boolean isAuthenticated() {
                return true;
            }

            @Override
            public Project getProjectEntityWithGroup(UUID projectId, List<UUID> leads, List<UUID> qaTaEngineers, List<UUID> devOpsEngineers, List<UUID> atpRunners, List<UUID> atpSupports, Permissions permissions) {
                return null;
            }

            @Override
            public boolean checkPoliciesForOperation(Project project, Operation operation) {
                return true;
            }

            @Override
            public boolean checkPoliciesForOperation(String entityName, Project project, Operation operation) {
                return true;
            }
        };
    }

    @Bean("userInfoProvider")
    public Provider<UserInfo> userInfoProvider() {
        return new DisableSecurityUserProvider();
    }

}
