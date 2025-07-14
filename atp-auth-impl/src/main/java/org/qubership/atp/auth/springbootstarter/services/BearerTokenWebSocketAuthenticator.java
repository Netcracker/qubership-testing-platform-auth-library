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

package org.qubership.atp.auth.springbootstarter.services;

import java.util.Set;

import org.keycloak.KeycloakPrincipal;
import org.keycloak.adapters.AdapterUtils;
import org.keycloak.adapters.KeycloakConfigResolver;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.RefreshableKeycloakSecurityContext;
import org.keycloak.adapters.rotation.AdapterTokenVerifier;
import org.keycloak.adapters.spi.KeycloakAccount;
import org.keycloak.adapters.springsecurity.account.SimpleKeycloakAccount;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.keycloak.common.VerificationException;
import org.keycloak.representations.AccessToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class BearerTokenWebSocketAuthenticator {

    private final KeycloakConfigResolver keycloakConfigResolver;

    /**
     * Authenticate web socket.
     * see https://stackoverflow.com/questions/50573461/spring-websockets-authentication-with-spring-security-and-keycloak
     *
     * @param tokenString the token string
     * @return the authentication
     * @throws AuthenticationException the authentication exception
     */
    public Authentication authenticate(String tokenString) throws AuthenticationException {
        KeycloakDeployment deployment = keycloakConfigResolver.resolve(null);
        AccessToken accessToken;

        try {
            accessToken = verifyAndGetAccessToken(tokenString, deployment);
        } catch (VerificationException e) {
            log.debug("Exception authenticating the token {}:", tokenString, e);
            throw new BadCredentialsException("Invalid token.", e);
        }

        return completeBearerAuthentication(deployment, tokenString, accessToken);
    }

    public AccessToken verifyAndGetAccessToken(String tokenString, KeycloakDeployment deployment)
            throws VerificationException {
        return AdapterTokenVerifier.verifyToken(tokenString, deployment);
    }

    // see org.keycloak.adapters.springsecurity.authentication.SpringSecurityRequestAuthenticator
    // method completeBearerAuthentication
    private Authentication completeBearerAuthentication(KeycloakDeployment deployment, String tokenString,
                                                        AccessToken accessToken) {
        RefreshableKeycloakSecurityContext session
                = new RefreshableKeycloakSecurityContext(deployment, null, tokenString, accessToken, null, null, null);
        KeycloakPrincipal<RefreshableKeycloakSecurityContext>
                principal = new KeycloakPrincipal<>(AdapterUtils.getPrincipalName(deployment, accessToken), session);
        RefreshableKeycloakSecurityContext securityContext = principal.getKeycloakSecurityContext();
        Set<String> roles = AdapterUtils.getRolesFromSecurityContext(securityContext);
        KeycloakAccount account = new SimpleKeycloakAccount(principal, roles, securityContext);
        KeycloakAuthenticationToken keycloakToken = new KeycloakAuthenticationToken(account, false);
        SecurityContextHolder.getContext().setAuthentication(keycloakToken);
        return keycloakToken;
    }
}
