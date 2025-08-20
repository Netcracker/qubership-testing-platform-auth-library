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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@Profile("default")
public class SecurityConfiguration {

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
