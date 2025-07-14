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

package org.qubership.atp.auth.springbootstarter.security.filters;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.keycloak.adapters.AdapterDeploymentContext;
import org.keycloak.adapters.AuthenticatedActionsHandler;
import org.keycloak.adapters.OIDCHttpFacade;
import org.keycloak.adapters.spi.HttpFacade;
import org.keycloak.adapters.springsecurity.filter.KeycloakAuthenticatedActionsFilter;
import org.qubership.atp.auth.springbootstarter.security.facades.AnonymousSupportSimpleHttpFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;

public class AnonymousSupportKeycloakAuthenticatedActionsFilter extends KeycloakAuthenticatedActionsFilter {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(AnonymousSupportKeycloakAuthenticatedActionsFilter.class);

    private ApplicationContext applicationContext;
    private AdapterDeploymentContext deploymentContext;

    /**
     * Constructor.
     */
    public AnonymousSupportKeycloakAuthenticatedActionsFilter() {
        super();
    }

    /**
     * Init Filter Bean.
     */
    @Override
    protected void initFilterBean() {
        super.initFilterBean();
        deploymentContext = applicationContext.getBean(AdapterDeploymentContext.class);
    }

    /**
     * ApplicationContext Setter.
     *
     * @param applicationContext parameter
     * @throws BeansException in case some bean exception.
     */
    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
        super.setApplicationContext(applicationContext);
        this.applicationContext = applicationContext;
    }

    /**
     * DoFilter method.
     *
     * @param request ServletRequest object
     * @param response ServletResponse object
     * @param chain Chain of filters
     * @throws IOException in case some IO exception
     * @throws ServletException in case servlet processing exception.
     */
    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain)
            throws IOException, ServletException {
        HttpFacade facade = new AnonymousSupportSimpleHttpFacade((HttpServletRequest) request,
                (HttpServletResponse) response);
        AuthenticatedActionsHandler handler = new AuthenticatedActionsHandler(
                deploymentContext.resolveDeployment(facade), (OIDCHttpFacade) facade);
        boolean handled = handler.handledRequest();
        if (handled) {
            LOGGER.debug("Authenticated filter handled request: {}", ((HttpServletRequest) request).getRequestURI());
        } else {
            chain.doFilter(request, response);
        }
    }
}
