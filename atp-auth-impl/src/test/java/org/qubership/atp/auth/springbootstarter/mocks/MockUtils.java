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

package org.qubership.atp.auth.springbootstarter.mocks;

import org.keycloak.KeycloakPrincipal;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.representations.AccessToken;
import org.mockito.Mockito;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class MockUtils {
    public static void mockSecurityContextHolder(String expectedToken) {
        KeycloakSecurityContext keycloakSecurityContext = Mockito.mock(KeycloakSecurityContext.class);
        AccessToken token = Mockito.mock(AccessToken.class);
        Mockito.lenient().when(keycloakSecurityContext.getTokenString()).thenReturn(expectedToken);
        Mockito.when(keycloakSecurityContext.getToken()).thenReturn(token);
        //noinspection rawtypes
        KeycloakPrincipal principal = Mockito.mock(KeycloakPrincipal.class);
        Mockito.when(principal.getKeycloakSecurityContext()).thenReturn(keycloakSecurityContext);
        Authentication authentication = Mockito.mock(Authentication.class);
        Mockito.when(authentication.getPrincipal()).thenReturn(principal);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
