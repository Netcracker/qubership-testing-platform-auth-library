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

import java.util.Set;
import java.util.UUID;

import org.qubership.atp.auth.springbootstarter.entities.Operation;
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
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@Profile("disable-security")
public class DisableSecurityConfiguration extends WebSecurityConfigurerAdapter {

    /**
     * Content Security Policy to be applied.
     */
    @Value("${atp-auth.headers.content-security-policy:default-src 'self' *some-domain.some-cloud}")
    private String contentSecurityPolicy;

    /**
     * Allow all PolicyEnforcement, will be used where you do not need to check permissions.
     *
     * @return PolicyEnforcement object configured to allow all operations and all roles.
     */
    @Bean("entityAccess")
    public PolicyEnforcement entityAccessEnforcement() {
        return new PolicyEnforcement() {
            @Override
            public boolean checkAccess(final Set<UUID> projectIdSet, final String action) {
                return true;
            }

            @Override
            public boolean checkAccess(final UUID projectId, final Operation action) {
                return true;
            }

            @Override
            public boolean checkAccess(final String entityName, final UUID projectId, final Operation action) {
                return true;
            }

            @Override
            public boolean checkAccess(final String entityName, final Set<UUID> projectIdSet, final Operation action) {
                return true;
            }

            @Override
            public boolean checkAccess(final String entityName, final Set<UUID> projectIdSet, final String action) {
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
                                       final Set<UUID> objectIds,
                                       final Operation operation) {
                return true;
            }

            @Override
            public boolean isAdmin() {
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
            public boolean checkPoliciesForOperation(final Project project, final Operation operation) {
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
     * Create and return User Info Provider with disabled security.
     *
     * @return a new DisableSecurityUserProvider object.
     */
    @Bean("userInfoProvider")
    public Provider<UserInfo> userInfoProvider() {
        return new DisableSecurityUserProvider();
    }

    /**
     * Configure HttpSecurity as disabled security and all resources are permitted.
     *
     * @param http HttpSecurity object to be configured
     * @throws Exception in case various configuration exceptions.
     */
    @Override
    protected void configure(final HttpSecurity http) throws Exception {
        http
                .csrf()
                .disable();
        http
                .headers()
                .defaultsDisabled()
                .xssProtection().xssProtectionEnabled(false)
                .and()
                .contentSecurityPolicy(contentSecurityPolicy)
                .and().and()
                .authorizeRequests()
                .antMatchers("/**")
                .permitAll();
    }
}
