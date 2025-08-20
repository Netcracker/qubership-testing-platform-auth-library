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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@Profile(value = {"default", "disable-security"})
public class DisableSecurityConfiguration {

    @Value("${atp-auth.headers.content-security-policy:default-src 'self' *}")
    private String contentSecurityPolicy;
    /**
     * Allow all PolicyEnforcement, will be used if there is no need to check permissions.
     *
     * @return PolicyEnforcement bean.
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
            public boolean checkAccess(final String entityName,
                                       final Set<UUID> projectIdSet,
                                       final String action) {
                return true;
            }

            @Override
            public boolean checkAccess(final String entityName,
                                       final UUID projectId,
                                       final String action) {
                return true;
            }

            @Override
            public boolean checkAccess(final String entityName,
                                       final String projectId,
                                       final String action) {
                return true;
            }

            @Override
            public boolean checkAccess(final String entityName,
                                       final UUID projectId,
                                       final UUID objectId,
                                       final Operation operation) {
                return true;
            }

            @Override
            public boolean checkAccess(final String entityName,
                                       final UUID projectId,
                                       final UUID objectId,
                                       final String operation) {
                return true;
            }

            @Override
            public boolean checkAccess(final String entityName,
                                       final UUID projectId,
                                       final Set<UUID> objectIds,
                                       final Operation operation) {
                return true;
            }

            @Override
            public boolean checkAccess(final String entityName,
                                       final UUID projectId,
                                       final Set<UUID> objectIds,
                                       final String operation) {
                return true;
            }

            @Override
            public boolean checkExternalAccess(final String entityName,
                                               final UUID projectId,
                                               final Operation action) {
                return true;
            }

            @Override
            public boolean checkExternalAccess(final String projectId, final String action) {
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
            public Project getProjectEntityWithGroup(final UUID projectId,
                                                     final List<UUID> leads,
                                                     final List<UUID> qaTaEngineers,
                                                     final List<UUID> devOpsEngineers,
                                                     final List<UUID> atpRunners,
                                                     final List<UUID> atpSupports,
                                                     final Permissions permissions) {
                return null;
            }

            @Override
            public boolean checkPoliciesForOperation(final Project project,
                                                     final Operation operation) {
                return true;
            }

            @Override
            public boolean checkPoliciesForOperation(final String entityName,
                                                     final Project project,
                                                     final Operation operation) {
                return true;
            }
        };
    }

    /**
     * Get User Info Provider.
     *
     * @return new DisableSecurityUserProvider instance.
     */
    @Bean("userInfoProvider")
    public Provider<UserInfo> userInfoProvider() {
        return new DisableSecurityUserProvider();
    }

    /**
     * Configure HTTP Security.
     *
     * @param http HTTP Security
     * @throws Exception Exception
     */
    public void configureHttpSecurity(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .headers(headers -> headers
                                .defaultsDisabled()
                                .contentSecurityPolicy(policy -> policy.policyDirectives(contentSecurityPolicy))
                )
                .authorizeHttpRequests(registry -> registry.requestMatchers("/**").permitAll()
                );
    }

    /**
     * Filter chain.
     *
     * @param http HTTPSecurity object
     * @return Security Filter Chain
     * @throws Exception exception in case configuration problems.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        configureHttpSecurity(http);
        return http.build();
    }
}
