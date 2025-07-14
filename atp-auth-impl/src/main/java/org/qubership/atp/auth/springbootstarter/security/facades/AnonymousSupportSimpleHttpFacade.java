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

package org.qubership.atp.auth.springbootstarter.security.facades;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.keycloak.KeycloakSecurityContext;
import org.keycloak.adapters.springsecurity.facade.SimpleHttpFacade;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

public class AnonymousSupportSimpleHttpFacade extends SimpleHttpFacade {

    /**
     * Constructor.
     *
     * @param request HttpServletRequest parameter
     * @param response HttpServletResponse parameter.
     */
    public AnonymousSupportSimpleHttpFacade(final HttpServletRequest request,
                                            final HttpServletResponse response) {
        super(request, response);
    }

    /**
     * Get KeycloakSecurityContext from SecurityContextHolder.
     *
     * @return KeycloakSecurityContext object.
     */
    @Override
    public KeycloakSecurityContext getSecurityContext() {
        SecurityContext context = SecurityContextHolder.getContext();
        if (context != null && context.getAuthentication() != null
                && KeycloakAuthenticationToken.class
                .isAssignableFrom(context.getAuthentication().getClass())) {
            KeycloakAuthenticationToken authentication = (KeycloakAuthenticationToken) context
                    .getAuthentication();
            return authentication.getAccount().getKeycloakSecurityContext();
        }
        return null;
    }
}
