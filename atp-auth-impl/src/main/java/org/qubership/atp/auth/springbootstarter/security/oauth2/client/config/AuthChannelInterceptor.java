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

package org.qubership.atp.auth.springbootstarter.security.oauth2.client.config;

import org.qubership.atp.auth.springbootstarter.services.BearerTokenWebSocketAuthenticator;
import org.qubership.atp.auth.springbootstarter.utils.BearerTokenStompExtractor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class AuthChannelInterceptor implements ChannelInterceptor {

    private final BearerTokenWebSocketAuthenticator webSocketAuthenticatorService;
    private final BearerTokenStompExtractor bearerTokenStompExtractor;

    @Override
    //see https://docs.spring.io/spring-framework/docs/current/reference/html/web.html#websocket-stomp-authentication-token-based
    public Message<?> preSend(Message<?> message, MessageChannel channel) throws MessagingException {
        log.debug("start preSend(message: {}, channel: {})", message, channel);
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        log.debug("accessor {}", accessor);
        if (accessor == null) {
            return message;
        }
        log.debug("accessor's command {}", accessor.getCommand());
        if (StompCommand.CONNECT == accessor.getCommand()) {
            String token = bearerTokenStompExtractor.extract(accessor);
            Authentication user;
            try {
                user = webSocketAuthenticatorService.authenticate(token);
            } catch (AuthenticationException e) {
                log.error("Authentication failed.", e);
                throw new MessagingException("Authentication failed. " + e.getMessage());
            }
            log.debug("user {}", user);
            accessor.setUser(user);
        }
        return message;
    }
}
