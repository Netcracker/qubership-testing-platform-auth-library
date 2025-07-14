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

import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.security.Principal;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.security.auth.Subject;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.qubership.atp.auth.springbootstarter.services.BearerTokenWebSocketAuthenticator;
import org.qubership.atp.auth.springbootstarter.utils.BearerTokenStompExtractor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;

class AuthChannelInterceptorTest {

    private BearerTokenWebSocketAuthenticator webSocketAuthenticatorService;
    private final BearerTokenStompExtractor bearerTokenStompExtractor = new BearerTokenStompExtractor();
    private MessageChannel channel;

    private AuthChannelInterceptor authChannelInterceptor;

    private StompHeaderAccessor connectMessageHeaders;
    private Authentication authentication;

    private final String userName = "admin";
    private final String token =
            "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6IC";
    private final byte[] bytePayload = new byte[]{};


    @BeforeEach
    void setUp() {
        webSocketAuthenticatorService = mock(BearerTokenWebSocketAuthenticator.class);
        channel = mock(MessageChannel.class);

        authChannelInterceptor =
                new AuthChannelInterceptor(webSocketAuthenticatorService, bearerTokenStompExtractor);

        Map<String, List<String>> headers = new HashMap<>();
        List<String> authorization = new LinkedList<>();
        authorization.add("Bearer " + token);
        headers.put("Authorization", authorization);

        connectMessageHeaders = StompHeaderAccessor.create(StompCommand.CONNECT, headers);
        connectMessageHeaders.setLeaveMutable(true);

        authentication = new AbstractAuthenticationToken(Collections.EMPTY_LIST) {
            @Override
            public boolean implies(Subject subject) {
                return false;
            }

            @Override
            public Object getCredentials() {
                return null;
            }

            @Override
            public Object getPrincipal() {
                return userName;
            }
        };
    }

    @Test
    void preSend_successAuthenticate() {
        when(webSocketAuthenticatorService.authenticate(token)).thenReturn(authentication);

        Message message = new GenericMessage<>(bytePayload, connectMessageHeaders.getMessageHeaders());
        authChannelInterceptor.preSend(message, channel);

        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        Principal user = accessor.getUser();
        Assert.assertNotNull(user);
        Assert.assertEquals(authentication, user);
    }

    @Test
    void preSend_failedAuthenticate() {
        when(webSocketAuthenticatorService.authenticate(token))
                .thenThrow(new BadCredentialsException("Invalid token."));

        Message message = new GenericMessage<>(bytePayload, connectMessageHeaders.getMessageHeaders());
        try {
            authChannelInterceptor.preSend(message, channel);
        } catch (MessagingException e) {
            Assert.assertEquals("Authentication failed. Invalid token.", e.getMessage());
            return;
        }

        fail("Authenticate should be failed");
    }
}