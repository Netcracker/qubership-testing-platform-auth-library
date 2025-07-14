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

package org.qubership.atp.auth.springbootstarter.security.oauth2.client.relay;

import org.keycloak.KeycloakPrincipal;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.DefaultOAuth2ClientContext;
import org.springframework.security.oauth2.client.token.AccessTokenRequest;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;

/**
 * This context unlike {@link DefaultOAuth2ClientContext} not requests token using credentials, it
 * gets the existed token from current authentication. If there is no token in current
 * authentication, {@link TokenRelayException} will be thrown.
 */
public class TokenRelayKeycloakClientContext extends DefaultOAuth2ClientContext {
    public TokenRelayKeycloakClientContext(AccessTokenRequest accessTokenRequest) {
        super(accessTokenRequest);
    }

    @Override
    public OAuth2AccessToken getAccessToken() {
        return changeTokenType(extractAccessToken(SecurityContextHolder.getContext().getAuthentication()));
    }

    private OAuth2AccessToken extractAccessToken(Authentication authentication) {

        if (authentication == null) {
            throw new TokenRelayException("Unable extract token, authentication is null");
        }

        if (authentication instanceof KeycloakAuthenticationToken) {
            KeycloakPrincipal principal = (KeycloakPrincipal) authentication.getPrincipal();
            KeycloakSecurityContext securityContext = principal.getKeycloakSecurityContext();
            String token = securityContext.getTokenString();

            return new DefaultOAuth2AccessToken(token);
        }

        throw new TokenRelayException("Unable extract token from authentication of type "
                + authentication.getClass().getName());

    }

    /**
     * Change token type to Bearer.
     */
    private OAuth2AccessToken changeTokenType(OAuth2AccessToken accessToken) {
        if (accessToken != null && "bearer".equals(accessToken.getTokenType())) {
            DefaultOAuth2AccessToken fixedToken = new DefaultOAuth2AccessToken(accessToken);
            fixedToken.setTokenType(OAuth2AccessToken.BEARER_TYPE);
            return fixedToken;
        }
        return accessToken;
    }
}
