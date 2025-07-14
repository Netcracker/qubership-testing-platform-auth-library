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

package org.qubership.atp.auth.springbootstarter.provider;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.qubership.atp.auth.springbootstarter.mocks.MockUtils.mockSecurityContextHolder;

import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.representations.AccessToken;
import org.mockito.junit.MockitoJUnitRunner;
import org.qubership.atp.auth.springbootstarter.entities.UserInfo;
import org.qubership.atp.auth.springbootstarter.provider.impl.UserProvider;
import org.springframework.security.core.context.SecurityContextHolder;

@RunWith(MockitoJUnitRunner.class)
public class UserProviderTest {

    @Test
    public void getTest() {
        // given
        String techUserId = UUID.randomUUID().toString();
        String techUserName = "techUserName";
        String techUserEmail = "example@example.com";
        String expectedToken = UUID.randomUUID().toString();
        mockSecurityContextHolder(expectedToken);

        KeycloakPrincipal keycloakPrincipal =
                (KeycloakPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        AccessToken accessToken = keycloakPrincipal.getKeycloakSecurityContext().getToken();
        // when
        when(keycloakPrincipal.toString()).thenReturn(techUserId);
        when(accessToken.getPreferredUsername()).thenReturn(techUserName);
        when(accessToken.getEmail()).thenReturn(techUserEmail);
        when(accessToken.getRealmAccess()).thenReturn(mock(AccessToken.Access.class));
        // then
        UserProvider userProvider = new UserProvider();
        UserInfo userInfo = userProvider.get();
        assertEquals(techUserId, userInfo.getId().toString());
        assertEquals(techUserName, userInfo.getUsername());
        assertEquals(techUserEmail, userInfo.getEmail());
    }
}
