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

package org.qubership.atp.auth.springbootstarter.provider.impl;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import org.keycloak.KeycloakPrincipal;
import org.qubership.atp.auth.springbootstarter.entities.UserInfo;
import org.qubership.atp.auth.springbootstarter.ssl.Provider;
import org.springframework.security.core.context.SecurityContextHolder;

public class UserProvider implements Provider<UserInfo> {

    /**
     * User id for anonymous requests.
     */
    private final UUID uuid = UUID.fromString("16df9e34-cf21-4360-b89c-785a4ed8f57d");

    /**
     * Get User Info from SecurityContextHolder.
     *
     * @return UserInfo object created and filled.
     */
    @Override
    public UserInfo get() {
        Object principalObject = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserInfo user = new UserInfo();

        if (principalObject instanceof KeycloakPrincipal) {
            KeycloakPrincipal principal = (KeycloakPrincipal) principalObject;
            String id = principal.toString();
            String name = principal.getKeycloakSecurityContext().getToken().getName();
            if (Objects.isNull(name)) {
                name = principal.getKeycloakSecurityContext().getToken().getPreferredUsername();
            }
            String firstName = principal.getKeycloakSecurityContext().getToken().getFamilyName();
            String lastName = principal.getKeycloakSecurityContext().getToken().getGivenName();
            String email = principal.getKeycloakSecurityContext().getToken().getEmail();
            Set<String> roles = principal.getKeycloakSecurityContext().getToken().getRealmAccess().getRoles();
            user.setId(UUID.fromString(id));
            user.setUsername(name);
            user.setFirstName(lastName);
            user.setLastName(firstName);
            user.setEmail(email);
            user.setRoles(new ArrayList<>(roles));
        } else {
            user.setId(uuid);
            user.setUsername((String) principalObject);
        }
        return user;
    }
}
