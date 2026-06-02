/*
 * # Copyright 2024-2026 NetCracker Technology Corporation
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

import static org.qubership.atp.auth.springbootstarter.Constants.AUTHORIZATION_HEADER_NAME;
import static org.qubership.atp.auth.springbootstarter.Constants.BEARER_TOKEN_TYPE;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.Strings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Oauth2FeignClientInterceptor implements RequestInterceptor {

    /**
     * Manager for obtaining OAuth2 tokens.
     */
    private final OAuth2AuthorizedClientManager authorizedClientManager;

    /**
     * Registration ID of the OAuth2 client (from application.yml).
     */
    private final String m2mRegistrationId;

    /**
     * Tracer bean.
     */
    private final Tracer tracer;

    /**
     * Expiry/refresh timeout in seconds.
     */
    @Value("${atp-auth.refreshTimeBeforeExpirationInSec:300}")
    private Integer refreshTimeBeforeExpiration;

    /**
     * Cached authorized client to avoid unnecessary token requests.
     */
    private OAuth2AuthorizedClient cachedM2MClient;

    /**
     * Constructor for interceptor with M2M support.
     *
     * @param authorizedClientManager OAuth2AuthorizedClientManager for M2M tokens
     * @param m2mRegistrationId       Registration ID for M2M client (from application.yml)
     * @param tracer                  tracer
     */
    public Oauth2FeignClientInterceptor(
            OAuth2AuthorizedClientManager authorizedClientManager,
            String m2mRegistrationId,
            Tracer tracer) {
        this.authorizedClientManager = authorizedClientManager;
        this.m2mRegistrationId = m2mRegistrationId;
        this.tracer = tracer;
    }

    /**
     * Apply changes to requestTemplate parameter object.
     *
     * @param requestTemplate RequestTemplate object to process.
     */
    @Override
    public void apply(RequestTemplate requestTemplate) {
        log.debug("start apply [requestTemplate.path={}]", requestTemplate.path());

        // 1. Try to get user token...
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuthenticationToken) {
            log.debug("JWT authentication token is present");
            Jwt token = jwtAuthenticationToken.getToken();
            String tokenValue = token.getTokenValue();
            if (isAccessTokenExpired(token)) {
                log.debug("Access token is expired or almost expired. Will try M2M token instead");
                // If user token isn't valid, try M2M (go further)...
            } else {
                log.debug("User token found and valid");
                setAuthorizationHeader(requestTemplate, tokenValue);
                return;
            }
        } else {
            log.debug("No valid JWT authentication token found");
        }

        // 2. M2M token path
        Optional<Span> nextSpan = Optional.ofNullable(tracer).map(Tracer::nextSpan);
        try {
            log.debug("Get m2m token");
            nextSpan.ifPresent(span -> {
                span.name("get m2m token");
                span.start();
            });

            String m2mToken = obtainM2MToken();
            if (m2mToken != null && !m2mToken.isEmpty()) {
                setAuthorizationHeader(requestTemplate, m2mToken);
                log.debug("M2M token successfully obtained and set");
            } else {
                log.warn("Failed to obtain M2M token");
            }
        } catch (Exception e) {
            log.error("Failed to obtain m2m token", e);
            throw e;
        } finally {
            nextSpan.ifPresent(Span::end);
        }
    }

    /**
     * Obtain M2M token using OAuth2AuthorizedClientManager.
     * Implements caching similar to the old accessTokenRequest.getExistingToken().
     *
     * @return access token string
     */
    private synchronized String obtainM2MToken() {
        // Check cached token (analogous to existingToken check in old code)
        if (cachedM2MClient != null) {
            OAuth2AccessToken token = cachedM2MClient.getAccessToken();
            if (!isM2MTokenExpired(token)) {
                log.debug("Reusing cached M2M token, expires at: {}", token.getExpiresAt());
                return token.getTokenValue();
            } else {
                log.debug("M2M token is expired or almost expired. Will request new token");
                cachedM2MClient = null; // Clear cache
            }
        }

        // Request new token
        OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest
                .withClientRegistrationId(m2mRegistrationId)
                .principal(m2mRegistrationId) // for client_credentials, principal can be registration ID
                .build();

        OAuth2AuthorizedClient authorizedClient = authorizedClientManager.authorize(authorizeRequest);

        if (authorizedClient != null) {
            cachedM2MClient = authorizedClient;
            OAuth2AccessToken token = authorizedClient.getAccessToken();
            log.debug("Obtained new M2M token, expires at: {}", token.getExpiresAt());
            return token.getTokenValue();
        }
        return null;
    }

    /**
     * Check if M2M token is expired or almost expired.
     * Mimics the old logic:
     *  existingToken.isExpired() || existingToken.getExpiresIn() < refreshTimeBeforeExpiration.
     */
    private boolean isM2MTokenExpired(OAuth2AccessToken token) {
        if (token == null || token.getExpiresAt() == null) {
            return true;
        }

        Instant now = Instant.now();
        Instant expiresAt = token.getExpiresAt();

        // If already expired
        if (now.isAfter(expiresAt)) {
            return true;
        }

        // Check if token will expire within the buffer period
        long expiresInSeconds = Duration.between(now, expiresAt).getSeconds();
        return expiresInSeconds < refreshTimeBeforeExpiration;
    }

    /**
     * Check if JWT token is expired or almost expired.
     */
    private boolean isAccessTokenExpired(Jwt token) {
        if (token == null || token.getExpiresAt() == null) {
            return true;
        }
        long expiresInMillis = token.getExpiresAt().toEpochMilli() - System.currentTimeMillis();
        return expiresInMillis < refreshTimeBeforeExpiration * 1000L;
    }

    /**
     * Add or replace bearer token.
     *
     * @param requestTemplate RequestTemplate object to process
     * @param token           String token value (Bearer token).
     */
    public void setAuthorizationHeader(final RequestTemplate requestTemplate, final String token) {
        String newTokenString = "%s %s".formatted(BEARER_TOKEN_TYPE, token);
        Collection<String> authorizationHeaderValues = requestTemplate.headers().get(AUTHORIZATION_HEADER_NAME);
        boolean authorizationHeaderHasToken = authorizationHeaderValues != null &&
                authorizationHeaderValues.stream()
                .anyMatch(value -> Strings.CI.startsWith(value, BEARER_TOKEN_TYPE));
        if (authorizationHeaderHasToken) {
            authorizationHeaderValues = authorizationHeaderValues.stream()
                            .filter(value -> !Strings.CI.startsWith(value, BEARER_TOKEN_TYPE))
                            .collect(Collectors.toList());
            // Remove header, then add a new one (having modified authorizationHeaderValues collection)
            requestTemplate.removeHeader(AUTHORIZATION_HEADER_NAME);
            authorizationHeaderValues.add(newTokenString);
            requestTemplate.header(AUTHORIZATION_HEADER_NAME, authorizationHeaderValues);
        } else {
            // Simply add new token (we checked above that no old Bearer token in the header values collection)
            requestTemplate.header(AUTHORIZATION_HEADER_NAME, newTokenString);
        }
    }
}
