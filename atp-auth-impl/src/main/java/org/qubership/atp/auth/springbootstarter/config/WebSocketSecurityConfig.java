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

import static org.springframework.messaging.simp.SimpMessageType.CONNECT;
import static org.springframework.messaging.simp.SimpMessageType.DISCONNECT;
import static org.springframework.messaging.simp.SimpMessageType.UNSUBSCRIBE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.keycloak.adapters.springsecurity.authentication.KeycloakAuthenticationProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.vote.AffirmativeBased;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.messaging.MessageSecurityMetadataSourceRegistry;
import org.springframework.security.config.annotation.web.socket.AbstractSecurityWebSocketMessageBrokerConfigurer;
import org.springframework.security.core.authority.mapping.SimpleAuthorityMapper;
import org.springframework.security.messaging.access.expression.DefaultMessageSecurityExpressionHandler;
import org.springframework.security.messaging.access.expression.MessageExpressionVoter;
import org.springframework.security.messaging.access.intercept.ChannelSecurityInterceptor;
import org.springframework.security.messaging.access.intercept.MessageSecurityMetadataSource;

@Configuration
@Profile("default")
public class WebSocketSecurityConfig extends AbstractSecurityWebSocketMessageBrokerConfigurer {

    @Override
    protected void configureInbound(MessageSecurityMetadataSourceRegistry messages) {
        messages
                .simpTypeMatchers(CONNECT, UNSUBSCRIBE, DISCONNECT).permitAll()
                .anyMessage().authenticated();
    }

    /**
     * Create ChannelSecurityInterceptor with custom authentication manager.
     *
     * @param messageSecurityMetadataSource the messageSecurityMetadataSource
     * @return ChannelSecurityInterceptor
     */
    @Bean
    public ChannelSecurityInterceptor inboundChannelSecurity(
            MessageSecurityMetadataSource messageSecurityMetadataSource) {
        ChannelSecurityInterceptor channelSecurityInterceptor =
                new ChannelSecurityInterceptor(messageSecurityMetadataSource);
        MessageExpressionVoter<Object> voter = new MessageExpressionVoter();
        voter.setExpressionHandler(new DefaultMessageSecurityExpressionHandler());
        List<AccessDecisionVoter<?>> voters = new ArrayList();
        voters.add(voter);
        AffirmativeBased manager = new AffirmativeBased(voters);
        channelSecurityInterceptor.setAccessDecisionManager(manager);
        channelSecurityInterceptor.setAuthenticationManager(
                new ProviderManager(Collections.singletonList(keycloakAuthenticationProvider())));
        return channelSecurityInterceptor;
    }

    protected KeycloakAuthenticationProvider keycloakAuthenticationProvider() {
        KeycloakAuthenticationProvider keycloakAuthenticationProvider = new KeycloakAuthenticationProvider();
        SimpleAuthorityMapper converter = new SimpleAuthorityMapper();
        converter.setConvertToUpperCase(true);
        keycloakAuthenticationProvider.setGrantedAuthoritiesMapper(converter);
        return keycloakAuthenticationProvider;
    }

    @Override
    protected boolean sameOriginDisabled() {
        // disable CSRF within WebSockets
        // see https://docs.spring.io/spring-security/site/docs/current/reference/html5/#websocket-sameorigin-spring
        return true;
    }
}
