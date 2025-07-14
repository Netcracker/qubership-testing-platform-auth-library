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

import static org.mockito.ArgumentMatchers.any;
import static org.qubership.atp.auth.springbootstarter.Constants.AUTHORIZATION_HEADER_NAME;
import static org.qubership.atp.auth.springbootstarter.Constants.BEARER_TOKEN_TYPE;
import static org.qubership.atp.auth.springbootstarter.mocks.MockUtils.mockSecurityContextHolder;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.representations.AccessToken;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.token.AccessTokenProvider;
import org.springframework.security.oauth2.client.token.AccessTokenRequest;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.test.util.ReflectionTestUtils;

import feign.RequestTemplate;

class Oauth2FeignClientInterceptorTest {

    private final String REFRESH_TIME_FIELD = "refreshTimeBeforeExpiration";

    @Test
    void setAuthorizationHeader_AuthorizationHeaderIsEmpty_ReturnOneToken() {
        Oauth2FeignClientInterceptor interceptor = new Oauth2FeignClientInterceptor(
                null,
                null,
                null
        );
        String expectedToken = UUID.randomUUID().toString();

        RequestTemplate requestTemplate = new RequestTemplate();
        interceptor.setAuthorizationHeader(requestTemplate, expectedToken);

        Collection<String> authorizationHeaders = requestTemplate.headers().get(AUTHORIZATION_HEADER_NAME);
        Assertions.assertEquals(1, authorizationHeaders.size());
        Assertions.assertEquals(String.format("%s %s", BEARER_TOKEN_TYPE, expectedToken),
                authorizationHeaders.iterator().next());
    }

    @Test
    void setAuthorizationHeader_AuthorizationHeaderHasSameToken_ReturnOneToken() {
        Oauth2FeignClientInterceptor interceptor = new Oauth2FeignClientInterceptor(
                null,
                null,
                null
        );
        String expectedToken = UUID.randomUUID().toString();

        RequestTemplate requestTemplate = new RequestTemplate();
        requestTemplate.header(AUTHORIZATION_HEADER_NAME, String.format("%s %s", "bearer", expectedToken));
        interceptor.setAuthorizationHeader(requestTemplate, expectedToken);

        Collection<String> authorizationHeaders = requestTemplate.headers().get(AUTHORIZATION_HEADER_NAME);
        Assertions.assertEquals(1, authorizationHeaders.size());
        Assertions.assertEquals(String.format("%s %s", BEARER_TOKEN_TYPE, expectedToken),
                authorizationHeaders.iterator().next());
    }

    @Test
    void apply_TokenIsActual_ReturnTokenFromKeycloakContext() {
        Oauth2FeignClientInterceptor interceptor = new Oauth2FeignClientInterceptor(
                null,
                null,
                null
        );
        String expectedToken = UUID.randomUUID().toString();
        mockSecurityContextHolder(expectedToken);
        RequestTemplate requestTemplate = new RequestTemplate();
        requestTemplate.header(AUTHORIZATION_HEADER_NAME, String.format("%s %s", "bearer", expectedToken));
        interceptor.apply(requestTemplate);

        Collection<String> authorizationHeaders = requestTemplate.headers().get(AUTHORIZATION_HEADER_NAME);
        Assertions.assertEquals(1, authorizationHeaders.size());
        Assertions.assertEquals(String.format("%s %s", BEARER_TOKEN_TYPE, expectedToken),
                authorizationHeaders.iterator().next());
    }

    @Test
    void apply_TokenIsExpired_ReturnNewOAuth2AccessToken() {
        OAuth2AccessToken oAuth2AccessExpectedToken = Mockito.mock(OAuth2AccessToken.class);
        String expectedToken = UUID.randomUUID().toString();
        Mockito.when(oAuth2AccessExpectedToken.getValue()).thenReturn(expectedToken);

        AccessTokenProvider accessTokenProvider = Mockito.mock(AccessTokenProvider.class);
        Mockito.when(accessTokenProvider.obtainAccessToken(any(), any())).thenReturn(oAuth2AccessExpectedToken);
        Oauth2FeignClientInterceptor interceptor = new Oauth2FeignClientInterceptor(
                accessTokenProvider,
                null,
                null
        );
        KeycloakSecurityContext keycloakSecurityContext = Mockito.mock(KeycloakSecurityContext.class);
        AccessToken token = Mockito.mock(AccessToken.class);

        Mockito.when(keycloakSecurityContext.getToken()).thenReturn(token);
        Mockito.when(token.isExpired()).thenReturn(true);
        //noinspection rawtypes
        KeycloakPrincipal principal = Mockito.mock(KeycloakPrincipal.class);
        Mockito.when(principal.getKeycloakSecurityContext()).thenReturn(keycloakSecurityContext);
        Authentication authentication = Mockito.mock(Authentication.class);
        Mockito.when(authentication.getPrincipal()).thenReturn(principal);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        RequestTemplate requestTemplate = new RequestTemplate();
        interceptor.apply(requestTemplate);
        Collection<String> authorizationHeaders = requestTemplate.headers().get(AUTHORIZATION_HEADER_NAME);
        Assertions.assertEquals(1, authorizationHeaders.size());
        Assertions.assertEquals(String.format("%s %s", BEARER_TOKEN_TYPE, expectedToken),
                authorizationHeaders.iterator().next());
    }

    @Test
    void apply_TokenIsAlmostExpired_ReturnNewOAuth2AccessToken() {
        OAuth2AccessToken oAuth2AccessExpectedToken = Mockito.mock(OAuth2AccessToken.class);
        String expectedToken = UUID.randomUUID().toString();
        Mockito.when(oAuth2AccessExpectedToken.getValue()).thenReturn(expectedToken);

        AccessTokenProvider accessTokenProvider = Mockito.mock(AccessTokenProvider.class);
        Mockito.when(accessTokenProvider.obtainAccessToken(any(), any())).thenReturn(oAuth2AccessExpectedToken);
        Oauth2FeignClientInterceptor interceptor = new Oauth2FeignClientInterceptor(
                accessTokenProvider,
                null,
                null
        );
        KeycloakSecurityContext keycloakSecurityContext = Mockito.mock(KeycloakSecurityContext.class);
        AccessToken token = Mockito.mock(AccessToken.class);

        int refreshTime = 300;
        Mockito.when(keycloakSecurityContext.getToken()).thenReturn(token);
        Mockito.when(token.isExpired()).thenReturn(false);
        int currentTimeInSeconds = Long.valueOf(System.currentTimeMillis() / 1000L).intValue();
        Mockito.when(token.getExpiration()).thenReturn(  currentTimeInSeconds + refreshTime - 1);
        ReflectionTestUtils.setField(interceptor, REFRESH_TIME_FIELD, refreshTime);
        KeycloakPrincipal principal = Mockito.mock(KeycloakPrincipal.class);
        Mockito.when(principal.getKeycloakSecurityContext()).thenReturn(keycloakSecurityContext);
        Authentication authentication = Mockito.mock(Authentication.class);
        Mockito.when(authentication.getPrincipal()).thenReturn(principal);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        RequestTemplate requestTemplate = new RequestTemplate();

        interceptor.apply(requestTemplate);

        Collection<String> authorizationHeaders = requestTemplate.headers().get(AUTHORIZATION_HEADER_NAME);
        Assertions.assertEquals(1, authorizationHeaders.size());
        Assertions.assertEquals(String.format("%s %s", BEARER_TOKEN_TYPE, expectedToken),
                authorizationHeaders.iterator().next());
    }

    @Test
    void apply_M2mToken_ReturnNewOAuth2AccessToken() {
        SecurityContextHolder.getContext().setAuthentication(null);
        String expectedToken = UUID.randomUUID().toString();
        AccessTokenProvider accessTokenProvider = Mockito.mock(AccessTokenProvider.class);
        OAuth2AccessToken accessToken = Mockito.mock(OAuth2AccessToken.class);
        Mockito.when(accessToken.getValue()).thenReturn(expectedToken);
        Mockito.when(accessTokenProvider.obtainAccessToken(any(), any())).thenReturn(accessToken);
        OAuth2ProtectedResourceDetails resourceDetails = Mockito.mock(OAuth2ProtectedResourceDetails.class);
        Oauth2FeignClientInterceptor interceptor = new Oauth2FeignClientInterceptor(
                accessTokenProvider,
                resourceDetails,
                null
        );
        RequestTemplate requestTemplate = new RequestTemplate();
        interceptor.apply(requestTemplate);
        Collection<String> authorizationHeaders = requestTemplate.headers().get(AUTHORIZATION_HEADER_NAME);
        Assertions.assertEquals(1, authorizationHeaders.size());
        Assertions.assertEquals(String.format("%s %s", BEARER_TOKEN_TYPE, expectedToken),
                authorizationHeaders.iterator().next());
    }

    @Test
    void apply_M2mTokenAlmostExpired_removeOldTokenAndSetNew() {
        SecurityContextHolder.getContext().setAuthentication(null);
        String expectedToken = UUID.randomUUID().toString();
        AccessTokenProvider accessTokenProvider = Mockito.mock(AccessTokenProvider.class);
        OAuth2AccessToken accessToken = Mockito.mock(OAuth2AccessToken.class);
        Mockito.when(accessToken.isExpired()).thenReturn(false);
        Mockito.when(accessToken.getValue()).thenReturn(expectedToken);
        Mockito.when(accessTokenProvider.obtainAccessToken(any(), any())).thenReturn(accessToken);
        OAuth2ProtectedResourceDetails resourceDetails = Mockito.mock(OAuth2ProtectedResourceDetails.class);
        AccessTokenRequest accessTokenRequest = Mockito.mock(AccessTokenRequest.class);
        Oauth2FeignClientInterceptor interceptor = new Oauth2FeignClientInterceptor(
                accessTokenProvider,
                resourceDetails,
                null,
                accessTokenRequest
        );
        int refreshTime = 300;
        ReflectionTestUtils.setField(interceptor, REFRESH_TIME_FIELD, refreshTime);
        OAuth2AccessToken existingToken = Mockito.mock(OAuth2AccessToken.class);
        Mockito.when(existingToken.getExpiresIn()).thenReturn(refreshTime -1);
        RequestTemplate requestTemplate = new RequestTemplate();
        Mockito.when(accessTokenRequest.getExistingToken()).thenReturn(existingToken);

        interceptor.apply(requestTemplate);

        ArgumentCaptor<OAuth2AccessToken> accessTokenCaptor = ArgumentCaptor.forClass(OAuth2AccessToken.class);
        Mockito.verify(accessTokenRequest, Mockito.times(2))
                .setExistingToken(accessTokenCaptor.capture());
        List<OAuth2AccessToken> capturedRequests = accessTokenCaptor.getAllValues();
        Assertions.assertTrue(capturedRequests.contains(null));
        Assertions.assertTrue(capturedRequests.contains(accessToken));
    }
}
