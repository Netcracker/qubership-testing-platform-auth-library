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

import static org.junit.Assert.fail;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.adapters.KeycloakConfigResolver;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.common.VerificationException;
import org.keycloak.representations.AccessToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.fasterxml.jackson.databind.ObjectMapper;

class BearerTokenWebSocketAuthenticatorTest {

    private KeycloakConfigResolver keycloakConfigResolver;

    private final String token =
            "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiw";
    private final String accessTokenJson =
            "{\"jti\":\"65ee147e-a113-4a72-9297-d53f2430314d\",\"exp\":1610463319,\"nbf\":0,\"iat\":1610459719,\"iss\":\"https://atp-keycloak-dev02.dev-atp-cloud.some-domain.com/auth/realms/atp2\",\"aud\":\"account\",\"sub\":\"3df73efb-db85-484f-902d-115805a7dc4d\",\"typ\":\"Bearer\",\"azp\":\"frontend\",\"nonce\":\"3\",\"auth_time\":1610459719,\"session_state\":\"577dbf94-8b92-4210-9599-93da39fe3836\",\"at_hash\":null,\"c_hash\":null,\"name\":\"Admin Adminovich\",\"given_name\":\"Admin\",\"family_name\":\"Adminovich\",\"middle_name\":null,\"nickname\":null,\"preferred_username\":\"admin\",\"profile\":null,\"picture\":null,\"website\":null,\"email\":\"test@test\",\"email_verified\":false,\"gender\":null,\"birthdate\":null,\"zoneinfo\":null,\"locale\":null,\"phone_number\":null,\"phone_number_verified\":null,\"address\":null,\"updated_at\":null,\"claims_locales\":null,\"acr\":\"1\",\"s_hash\":null,\"trusted-certs\":null,\"allowed-origins\":[\"*\"],\"realm_access\":{\"roles\":[\"offline_access\",\"ATP_ADMIN\",\"uma_authorization\"],\"verify_caller\":null},\"resource_access\":{\"account\":{\"roles\":[\"manage-account\",\"manage-account-links\",\"view-profile\"],\"verify_caller\":null}},\"authorization\":null,\"cnf\":null,\"scope\":\"email profile\"}";

    private KeycloakDeployment keycloakDeployment;

    private AccessToken accessToken;

    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() throws IOException {
        keycloakConfigResolver = mock(KeycloakConfigResolver.class);
        keycloakDeployment = mock(KeycloakDeployment.class);
        when(keycloakConfigResolver.resolve(null)).thenReturn(keycloakDeployment);

        accessToken = mapper.readValue(accessTokenJson, AccessToken.class);
    }

    @Test
    void authenticate_successAuthenticate() throws VerificationException {
        BearerTokenWebSocketAuthenticator bearerTokenWebSocketAuthenticator =
                new BearerTokenWebSocketAuthenticator(keycloakConfigResolver);

        BearerTokenWebSocketAuthenticator spyBearerTokenWebSocketAuthenticator = spy(bearerTokenWebSocketAuthenticator);

        doReturn(accessToken).when(spyBearerTokenWebSocketAuthenticator)
                .verifyAndGetAccessToken(token, keycloakDeployment);

        Authentication authentication = spyBearerTokenWebSocketAuthenticator.authenticate(token);
        Assert.assertNotNull(authentication);

        Authentication authenticationFromContext = SecurityContextHolder.getContext().getAuthentication();
        Assert.assertNotNull(authenticationFromContext);
    }


    @Test
    void authenticate_failedAuthenticate() throws VerificationException {
        BearerTokenWebSocketAuthenticator bearerTokenWebSocketAuthenticator =
                new BearerTokenWebSocketAuthenticator(keycloakConfigResolver);

        BearerTokenWebSocketAuthenticator spyBearerTokenWebSocketAuthenticator = spy(bearerTokenWebSocketAuthenticator);

        doThrow(new VerificationException("Some verification error.")).when(spyBearerTokenWebSocketAuthenticator)
                .verifyAndGetAccessToken(token, keycloakDeployment);

        try {
            spyBearerTokenWebSocketAuthenticator.authenticate(token);
        } catch (BadCredentialsException e) {
            Assert.assertEquals("Invalid token.", e.getMessage());
            return;
        }

        fail("Authenticate should be failed");
    }
}