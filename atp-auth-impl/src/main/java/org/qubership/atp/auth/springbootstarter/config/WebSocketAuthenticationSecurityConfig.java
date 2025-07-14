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

import org.keycloak.adapters.KeycloakConfigResolver;
import org.qubership.atp.auth.springbootstarter.security.oauth2.client.config.AuthChannelInterceptor;
import org.qubership.atp.auth.springbootstarter.services.BearerTokenWebSocketAuthenticator;
import org.qubership.atp.auth.springbootstarter.utils.BearerTokenStompExtractor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;


@Configuration
@Profile("default")
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
@EnableWebMvc
@EnableWebSecurity
//see https://docs.spring.io/spring-framework/docs/current/reference/html/web.html#websocket-stomp-authentication-token-based
public class WebSocketAuthenticationSecurityConfig implements WebSocketMessageBrokerConfigurer {

    @Autowired
    private KeycloakConfigResolver keycloakConfigResolver;

    @Autowired
    private BearerTokenStompExtractor bearerTokenStompExtractor;

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new AuthChannelInterceptor(
                new BearerTokenWebSocketAuthenticator(keycloakConfigResolver), bearerTokenStompExtractor));
    }
}
