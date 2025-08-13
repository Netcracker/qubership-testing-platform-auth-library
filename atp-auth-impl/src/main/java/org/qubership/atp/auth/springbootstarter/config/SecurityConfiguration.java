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

import org.keycloak.adapters.springsecurity.KeycloakConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.WebSecurityConfigurer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;

@KeycloakConfiguration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@Profile("default")
public class SecurityConfiguration extends WebSecurityConfigurerAdapter
        implements WebSecurityConfigurer<WebSecurity> {

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
     * Configure WebSecurity parameter object.
     *
     * @param web WebSecurity object to be configured
     * @throws Exception in case various configuration exceptions.
     */
    @Override
    public void configure(final WebSecurity web) throws Exception {
        super.configure(web);
        web
                .ignoring()
                .antMatchers("/assets/**")
                .antMatchers(HttpMethod.OPTIONS, "/**");
    }

    /**
     * Configure HttpSecurity.
     *
     * @param http HttpSecurity object to be configured
     * @throws Exception in case various configuration exceptions.
     */
    @Override
    protected void configure(final HttpSecurity http) throws Exception {
        super.configure(http);
        http
                .headers()
                .xssProtection().xssProtectionEnabled(false)
                .and()
                .contentSecurityPolicy(contentSecurityPolicy)
                .and()
                .frameOptions()
                .sameOrigin()
                .and()
                .csrf().disable()
                .authorizeRequests()
                .antMatchers("/ws/api").permitAll()
                .antMatchers("/api/public/**").permitAll()
                .antMatchers("/rest/deployment/**").permitAll()
                .antMatchers("/*/api/**", "/api/**").authenticated()
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    }
}
