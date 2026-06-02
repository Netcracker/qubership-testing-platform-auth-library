/*
 * # Copyright 2026-2028 NetCracker Technology Corporation
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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import feign.RequestTemplate;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class Oauth2FeignClientInterceptorTest {

    private static final String REGISTRATION_ID = "m2m-client";
    private static final String ACCESS_TOKEN_VALUE = "test-access-token-12345";
    private static final String AUTHORIZATION_HEADER_NAME = "Authorization";

    @Mock
    private OAuth2AuthorizedClientManager authorizedClientManager;

    @Mock
    private Tracer tracer;

    @Mock
    private Span span;

    @Mock
    private OAuth2AuthorizedClient authorizedClient;

    @Mock
    private OAuth2AccessToken accessToken;

    @Mock
    private Jwt jwt;

    private Oauth2FeignClientInterceptor interceptor;
    private RequestTemplate requestTemplate;

    @BeforeEach
    void setUp() throws Exception {
        requestTemplate = new RequestTemplate();
        interceptor = new Oauth2FeignClientInterceptor(
                authorizedClientManager,
                REGISTRATION_ID,
                tracer
        );

        // Set refreshTimeBeforeExpiration via reflection
        Field refreshField = Oauth2FeignClientInterceptor.class.getDeclaredField("refreshTimeBeforeExpiration");
        refreshField.setAccessible(true);
        refreshField.set(interceptor, 300);

        // Clear security context before each test
        SecurityContextHolder.clearContext();
    }

    // ==================== setAuthorizationHeader TESTS ====================

    @Test
    void setAuthorizationHeader_shouldAddBearerToken_whenNoAuthorizationHeaderExists() {
        // Given
        String token = "test-token";

        // When
        interceptor.setAuthorizationHeader(requestTemplate, token);

        // Then
        Map<String, Collection<String>> headers = requestTemplate.headers();
        assertTrue(headers.containsKey(AUTHORIZATION_HEADER_NAME));
        Collection<String> authHeaders = headers.get(AUTHORIZATION_HEADER_NAME);
        assertEquals(1, authHeaders.size());
        assertEquals("Bearer test-token", authHeaders.iterator().next());
    }

    @Test
    void setAuthorizationHeader_shouldReplaceExistingBearerToken_whenMultipleAuthorizationHeadersExist() {
        // Given
        String token = "new-token";
        requestTemplate.header(AUTHORIZATION_HEADER_NAME, "Basic old-credentials");
        requestTemplate.header(AUTHORIZATION_HEADER_NAME, "Bearer old-token");
        requestTemplate.header("Other-Header", "some-value");

        // When
        interceptor.setAuthorizationHeader(requestTemplate, token);

        // Then
        Map<String, Collection<String>> headers = requestTemplate.headers();
        Collection<String> authHeaders = headers.get(AUTHORIZATION_HEADER_NAME);

        // Should have two headers: Basic credentials AND new Bearer token
        assertEquals(2, authHeaders.size());

        // Check that Basic header remains (without checking exact order)
        assertTrue(authHeaders.stream().anyMatch(h -> h.startsWith("Basic")));

        // Check that new Bearer token is present (and old Bearer token is gone)
        assertTrue(authHeaders.stream().anyMatch(h -> h.equals("Bearer new-token")));
        assertFalse(authHeaders.stream().anyMatch(h -> h.equals("Bearer old-token")));    }

    @Test
    void setAuthorizationHeader_shouldAddBearerToken_whenOnlyNonBearerAuthorizationHeaderExists() {
        // Given
        String token = "new-token";
        requestTemplate.header(AUTHORIZATION_HEADER_NAME, "Basic credentials123");

        // When
        interceptor.setAuthorizationHeader(requestTemplate, token);

        // Then
        Map<String, Collection<String>> headers = requestTemplate.headers();
        Collection<String> authHeaders = headers.get(AUTHORIZATION_HEADER_NAME);

        // Now should have both Basic and Bearer
        assertEquals(2, authHeaders.size());
        assertTrue(authHeaders.stream().anyMatch(h -> h.startsWith("Basic")));
        assertTrue(authHeaders.stream().anyMatch(h -> h.startsWith("Bearer new-token")));
    }

    @Test
    void setAuthorizationHeader_shouldReplaceBearerToken_whenBearerTokenAlreadyExists() {
        // Given
        String newToken = "new-bearer-token";
        requestTemplate.header(AUTHORIZATION_HEADER_NAME, "Bearer old-token");

        // When
        interceptor.setAuthorizationHeader(requestTemplate, newToken);

        // Then
        Map<String, Collection<String>> headers = requestTemplate.headers();
        Collection<String> authHeaders = headers.get(AUTHORIZATION_HEADER_NAME);

        assertEquals(1, authHeaders.size());
        assertEquals("Bearer new-bearer-token", authHeaders.iterator().next());
    }

    // ==================== USER TOKEN TESTS ====================

    @Test
    void apply_shouldUseUserToken_whenValidJwtTokenPresent() {
        // Create real JwtAuthenticationToken with mocked Jwt inside
        Instant futureExpiry = Instant.now().plus(Duration.ofHours(1));
        when(jwt.getTokenValue()).thenReturn(ACCESS_TOKEN_VALUE);
        when(jwt.getExpiresAt()).thenReturn(futureExpiry);

        JwtAuthenticationToken jwtAuthToken = new JwtAuthenticationToken(jwt);
        SecurityContextHolder.getContext().setAuthentication(jwtAuthToken);

        interceptor.apply(requestTemplate);

        Map<String, Collection<String>> headers = requestTemplate.headers();
        assertTrue(headers.containsKey(AUTHORIZATION_HEADER_NAME));
        String authHeader = headers.get(AUTHORIZATION_HEADER_NAME).iterator().next();
        assertTrue(authHeader.contains(ACCESS_TOKEN_VALUE));

        verify(authorizedClientManager, never()).authorize(any());
    }

    @Test
    void apply_shouldFallbackToM2MToken_whenUserTokenIsExpired() {
        Instant pastExpiry = Instant.now().minus(Duration.ofMinutes(1));
        when(jwt.getTokenValue()).thenReturn(ACCESS_TOKEN_VALUE);
        when(jwt.getExpiresAt()).thenReturn(pastExpiry);

        JwtAuthenticationToken jwtAuthToken = new JwtAuthenticationToken(jwt);
        SecurityContextHolder.getContext().setAuthentication(jwtAuthToken);

        when(authorizedClientManager.authorize(any(OAuth2AuthorizeRequest.class)))
                .thenReturn(authorizedClient);
        when(authorizedClient.getAccessToken()).thenReturn(accessToken);
        when(accessToken.getTokenValue()).thenReturn("m2m-token");
        when(accessToken.getExpiresAt()).thenReturn(Instant.now().plus(Duration.ofHours(1)));

        interceptor.apply(requestTemplate);

        Map<String, Collection<String>> headers = requestTemplate.headers();
        assertTrue(headers.containsKey(AUTHORIZATION_HEADER_NAME));
        String authHeader = headers.get(AUTHORIZATION_HEADER_NAME).iterator().next();
        assertTrue(authHeader.contains("m2m-token"));

        verify(authorizedClientManager, times(1)).authorize(any());
    }

    @Test
    void apply_shouldFallbackToM2MToken_whenUserTokenIsNearExpiration() {
        Instant nearExpiry = Instant.now().plus(Duration.ofSeconds(200));
        when(jwt.getTokenValue()).thenReturn(ACCESS_TOKEN_VALUE);
        when(jwt.getExpiresAt()).thenReturn(nearExpiry);

        JwtAuthenticationToken jwtAuthToken = new JwtAuthenticationToken(jwt);
        SecurityContextHolder.getContext().setAuthentication(jwtAuthToken);

        when(authorizedClientManager.authorize(any(OAuth2AuthorizeRequest.class)))
                .thenReturn(authorizedClient);
        when(authorizedClient.getAccessToken()).thenReturn(accessToken);
        when(accessToken.getTokenValue()).thenReturn("m2m-token");
        when(accessToken.getExpiresAt()).thenReturn(Instant.now().plus(Duration.ofHours(1)));

        interceptor.apply(requestTemplate);

        Map<String, Collection<String>> headers = requestTemplate.headers();
        assertTrue(headers.containsKey(AUTHORIZATION_HEADER_NAME));
        String authHeader = headers.get(AUTHORIZATION_HEADER_NAME).iterator().next();
        assertTrue(authHeader.contains("m2m-token"));

        verify(authorizedClientManager, times(1)).authorize(any());
    }

    @Test
    void apply_shouldUseM2MToken_whenNoAuthenticationPresent() {
        SecurityContextHolder.clearContext();

        when(authorizedClientManager.authorize(any(OAuth2AuthorizeRequest.class)))
                .thenReturn(authorizedClient);
        when(authorizedClient.getAccessToken()).thenReturn(accessToken);
        when(accessToken.getTokenValue()).thenReturn("m2m-token");
        when(accessToken.getExpiresAt()).thenReturn(Instant.now().plus(Duration.ofHours(1)));

        interceptor.apply(requestTemplate);

        Map<String, Collection<String>> headers = requestTemplate.headers();
        assertTrue(headers.containsKey(AUTHORIZATION_HEADER_NAME));
        String authHeader = headers.get(AUTHORIZATION_HEADER_NAME).iterator().next();
        assertTrue(authHeader.contains("m2m-token"));
    }

    @Test
    void apply_shouldUseM2MToken_whenAuthenticationIsNotJwt() {
        // Create simple implementation of Authentication, which is NOT a JwtAuthenticationToken
        Authentication nonJwtAuth = new Authentication() {
            @Override
            public String getName() {
                return "test-user";
            }

            @Override
            public java.util.Collection<? extends org.springframework.security.core.GrantedAuthority> getAuthorities() {
                return java.util.Collections.emptyList();
            }

            @Override
            public Object getCredentials() {
                return null;
            }

            @Override
            public Object getDetails() {
                return null;
            }

            @Override
            public Object getPrincipal() {
                return "test-principal";
            }

            @Override
            public boolean isAuthenticated() {
                return true;
            }

            @Override
            public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
            }
        };

        SecurityContextHolder.getContext().setAuthentication(nonJwtAuth);

        when(authorizedClientManager.authorize(any(OAuth2AuthorizeRequest.class))).thenReturn(authorizedClient);
        when(authorizedClient.getAccessToken()).thenReturn(accessToken);
        when(accessToken.getTokenValue()).thenReturn("m2m-token");
        when(accessToken.getExpiresAt()).thenReturn(Instant.now().plus(Duration.ofHours(1)));

        interceptor.apply(requestTemplate);

        Map<String, Collection<String>> headers = requestTemplate.headers();
        assertTrue(headers.containsKey(AUTHORIZATION_HEADER_NAME));
        String authHeader = headers.get(AUTHORIZATION_HEADER_NAME).iterator().next();
        assertTrue(authHeader.contains("m2m-token"));

        verify(authorizedClientManager, times(1)).authorize(any());
    }

    // ==================== M2M TOKEN TESTS ====================

    @Test
    void apply_shouldObtainAndCacheM2MToken() {
        SecurityContextHolder.clearContext();

        when(authorizedClientManager.authorize(any(OAuth2AuthorizeRequest.class)))
                .thenReturn(authorizedClient);
        when(authorizedClient.getAccessToken()).thenReturn(accessToken);
        when(accessToken.getTokenValue()).thenReturn(ACCESS_TOKEN_VALUE);
        when(accessToken.getExpiresAt()).thenReturn(Instant.now().plus(Duration.ofHours(1)));

        // First call
        interceptor.apply(requestTemplate);

        // Second call with new template
        RequestTemplate secondRequest = new RequestTemplate();
        interceptor.apply(secondRequest);

        // Should only call authorize once (caching works)
        verify(authorizedClientManager, times(1)).authorize(any(OAuth2AuthorizeRequest.class));

        // Both requests should have the token
        assertTrue(requestTemplate.headers().get(AUTHORIZATION_HEADER_NAME).iterator().next().contains(ACCESS_TOKEN_VALUE));
        assertTrue(secondRequest.headers().get(AUTHORIZATION_HEADER_NAME).iterator().next().contains(ACCESS_TOKEN_VALUE));
    }

    @Test
    void apply_shouldRequestNewM2MToken_whenCachedTokenIsExpired() {
        SecurityContextHolder.clearContext();

        Instant expiredExpiry = Instant.now().minus(Duration.ofMinutes(1));

        // Mock for the 1st token
        OAuth2AccessToken firstToken = mock(OAuth2AccessToken.class);
        when(firstToken.getTokenValue()).thenReturn(ACCESS_TOKEN_VALUE);
        when(firstToken.getExpiresAt()).thenReturn(expiredExpiry);

        OAuth2AuthorizedClient firstAuthorizedClient = mock(OAuth2AuthorizedClient.class);
        when(firstAuthorizedClient.getAccessToken()).thenReturn(firstToken);

        // Mock for the 2nd token (after refresh)
        OAuth2AccessToken secondToken = mock(OAuth2AccessToken.class);
        String newTokenValue = "refreshed-m2m-token";
        when(secondToken.getTokenValue()).thenReturn(newTokenValue);
        when(secondToken.getExpiresAt()).thenReturn(Instant.now().plus(Duration.ofHours(1)));

        OAuth2AuthorizedClient secondAuthorizedClient = mock(OAuth2AuthorizedClient.class);
        when(secondAuthorizedClient.getAccessToken()).thenReturn(secondToken);

        // Configure sequential responses for authorize()
        when(authorizedClientManager.authorize(any(OAuth2AuthorizeRequest.class)))
                .thenReturn(firstAuthorizedClient)
                .thenReturn(secondAuthorizedClient);

        // The 1st request - get token (already expired)
        interceptor.apply(requestTemplate);

        // The 2nd request - should get a new token (because old is already expired)
        RequestTemplate secondRequest = new RequestTemplate();
        interceptor.apply(secondRequest);

        // Check that authorize() was invoked 2 times
        verify(authorizedClientManager, times(2)).authorize(any(OAuth2AuthorizeRequest.class));

        // Check that new token is used in the 2nd request
        Map<String, Collection<String>> headers = secondRequest.headers();
        String authHeaderValue = headers.get(AUTHORIZATION_HEADER_NAME).iterator().next();
        assertTrue(authHeaderValue.contains(newTokenValue));
    }

    @Test
    void apply_shouldRequestNewM2MToken_whenCachedTokenIsNearExpiration() {
        SecurityContextHolder.clearContext();

        Instant nearExpiry = Instant.now().plus(Duration.ofSeconds(200));

        // Mock for the 1st token
        OAuth2AccessToken firstToken = mock(OAuth2AccessToken.class);
        when(firstToken.getTokenValue()).thenReturn(ACCESS_TOKEN_VALUE);
        when(firstToken.getExpiresAt()).thenReturn(nearExpiry);

        OAuth2AuthorizedClient firstAuthorizedClient = mock(OAuth2AuthorizedClient.class);
        when(firstAuthorizedClient.getAccessToken()).thenReturn(firstToken);

        // Mock for the 2nd token (after refresh)
        OAuth2AccessToken secondToken = mock(OAuth2AccessToken.class);
        String newTokenValue = "refreshed-m2m-token";
        when(secondToken.getTokenValue()).thenReturn(newTokenValue);
        when(secondToken.getExpiresAt()).thenReturn(Instant.now().plus(Duration.ofHours(1)));

        OAuth2AuthorizedClient secondAuthorizedClient = mock(OAuth2AuthorizedClient.class);
        when(secondAuthorizedClient.getAccessToken()).thenReturn(secondToken);

        // Configure sequential responses for authorize()
        when(authorizedClientManager.authorize(any(OAuth2AuthorizeRequest.class)))
                .thenReturn(firstAuthorizedClient)
                .thenReturn(secondAuthorizedClient);

        // The 1st request - get token (will expire after 200 sec)
        interceptor.apply(requestTemplate);

        // The 2nd request - should get a new token (because old is near to expire)
        RequestTemplate secondRequest = new RequestTemplate();
        interceptor.apply(secondRequest);

        // Check that authorize() was invoked 2 times
        verify(authorizedClientManager, times(2)).authorize(any(OAuth2AuthorizeRequest.class));

        // Check that new token is used in the 2nd request
        Map<String, Collection<String>> headers = secondRequest.headers();
        String authHeaderValue = headers.get(AUTHORIZATION_HEADER_NAME).iterator().next();
        assertTrue(authHeaderValue.contains(newTokenValue));
    }

    @Test
    void apply_shouldNotAddAuthorizationHeader_whenM2MTokenObtainmentFails() {
        SecurityContextHolder.clearContext();

        when(authorizedClientManager.authorize(any(OAuth2AuthorizeRequest.class)))
                .thenReturn(null);

        interceptor.apply(requestTemplate);

        assertFalse(requestTemplate.headers().containsKey(AUTHORIZATION_HEADER_NAME));
        verify(authorizedClientManager, times(1)).authorize(any());
    }

    @Test
    void apply_shouldHandleException_whenM2MTokenObtainmentThrowsException() {
        SecurityContextHolder.clearContext();

        when(authorizedClientManager.authorize(any(OAuth2AuthorizeRequest.class)))
                .thenThrow(new RuntimeException("OAuth2 service unavailable"));

        // Expect that exception is thrown (old code behavior is preserved)
        RuntimeException thrown = assertThrows(RuntimeException.class, () -> interceptor.apply(requestTemplate));
        assertEquals("OAuth2 service unavailable", thrown.getMessage());

        // Header should not be added, because getting of token failed
        assertFalse(requestTemplate.headers().containsKey(AUTHORIZATION_HEADER_NAME));
    }

    /* Variant instead of above, in case "throw e" is removed from production code ('apply' method)
    void apply_shouldHandleException_whenM2MTokenObtainmentThrowsException() {
        SecurityContextHolder.clearContext();

        Mockito.when(authorizedClientManager.authorize(ArgumentMatchers.any(OAuth2AuthorizeRequest.class)))
                .thenThrow(new RuntimeException("OAuth2 service unavailable"));

        Assertions.assertDoesNotThrow(() -> interceptor.apply(requestTemplate));
        Assertions.assertFalse(requestTemplate.headers().containsKey(AUTHORIZATION_HEADER_NAME));
    }*/

    @Test
    void apply_shouldPassCorrectParametersToAuthorizeRequest() {
        SecurityContextHolder.clearContext();

        ArgumentCaptor<OAuth2AuthorizeRequest> requestCaptor =
                ArgumentCaptor.forClass(OAuth2AuthorizeRequest.class);

        when(authorizedClientManager.authorize(requestCaptor.capture()))
                .thenReturn(authorizedClient);
        when(authorizedClient.getAccessToken()).thenReturn(accessToken);
        when(accessToken.getTokenValue()).thenReturn(ACCESS_TOKEN_VALUE);
        when(accessToken.getExpiresAt()).thenReturn(Instant.now().plus(Duration.ofHours(1)));

        interceptor.apply(requestTemplate);

        OAuth2AuthorizeRequest capturedRequest = requestCaptor.getValue();
        assertEquals(REGISTRATION_ID, capturedRequest.getClientRegistrationId());
        assertEquals(REGISTRATION_ID, capturedRequest.getPrincipal().getName());
    }

    @Test
    void apply_shouldStartAndFinishTracingSpan() {
        SecurityContextHolder.clearContext();

        when(tracer.nextSpan()).thenReturn(span);
        when(authorizedClientManager.authorize(any(OAuth2AuthorizeRequest.class)))
                .thenReturn(authorizedClient);
        when(authorizedClient.getAccessToken()).thenReturn(accessToken);
        when(accessToken.getTokenValue()).thenReturn(ACCESS_TOKEN_VALUE);
        when(accessToken.getExpiresAt()).thenReturn(Instant.now().plus(Duration.ofHours(1)));

        interceptor.apply(requestTemplate);

        verify(span).name("get m2m token");
        verify(span).start();
        verify(span).end();
    }

    @Test
    void apply_shouldNotStartTracingSpan_whenTracerIsNull() {
        Oauth2FeignClientInterceptor interceptorWithoutTracer = new Oauth2FeignClientInterceptor(
                authorizedClientManager,
                REGISTRATION_ID,
                null
        );

        SecurityContextHolder.clearContext();

        when(authorizedClientManager.authorize(any(OAuth2AuthorizeRequest.class)))
                .thenReturn(authorizedClient);
        when(authorizedClient.getAccessToken()).thenReturn(accessToken);
        when(accessToken.getTokenValue()).thenReturn(ACCESS_TOKEN_VALUE);
        when(accessToken.getExpiresAt()).thenReturn(Instant.now().plus(Duration.ofHours(1)));

        assertDoesNotThrow(() -> interceptorWithoutTracer.apply(requestTemplate));

        Map<String, Collection<String>> headers = requestTemplate.headers();
        assertTrue(headers.containsKey(AUTHORIZATION_HEADER_NAME));
    }
}