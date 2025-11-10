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
import org.qubership.atp.common.logging.interceptor.RestTemplateLogInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@Profile("disable-security")
public class DisableSecurityConfiguration {

    /**
     * Service Name set in the service configuration.
     */
    @Value("${spring.application.name}")
    private String serviceName;

    /**
     * Content Security Policy to be applied.
     */
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
     * Return a simple {@link RestTemplate} instead of a RestTemplate that applies a user token to
     * each request.
     *
     * @param restTemplateLogInterceptor RestTemplateLogInterceptor object
     * @return RestTemplate with SimpleClientHttpRequestFactory and the interceptor added.
     */
    @Bean("relayRestTemplate")
    public RestTemplate relayRestTemplate(final RestTemplateLogInterceptor restTemplateLogInterceptor) {
        RestTemplate restTemplate =
                new RestTemplate(new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory()));
        restTemplate.getInterceptors().add(restTemplateLogInterceptor);
        return restTemplate;
    }

    /**
     * Return a simple {@link WebClient} instead of a webclient that applies a user token to each
     * request.
     *
     * @return simple WebClient.
     */
    @Bean("relayWebClient")
    public WebClient relayWebClient() {
        return WebClient.builder().build();
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
     * Configure Web Security as 'all resources are allowed'.
     *
     * @return WebSecurityCustomizer object built.
     */
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring().requestMatchers("/**");
    }

}
