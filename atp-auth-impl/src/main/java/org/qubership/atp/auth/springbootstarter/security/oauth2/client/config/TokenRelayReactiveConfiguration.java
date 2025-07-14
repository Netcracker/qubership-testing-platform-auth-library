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

package org.qubership.atp.auth.springbootstarter.security.oauth2.client.config;

import org.qubership.atp.auth.springbootstarter.security.oauth2.client.relay.TokenRelayKeycloakClientContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.security.oauth2.client.token.DefaultAccessTokenRequest;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@Profile("default")
public class TokenRelayReactiveConfiguration {
    private final TokenRelayKeycloakClientContext tokenContext = new TokenRelayKeycloakClientContext(
            new DefaultAccessTokenRequest());

    /**
     * Return {@link WebClient} which applies a user token to each request.
     */
    @Bean("relayWebClient")
    public WebClient relayWebClient(ClientHttpConnector clientHttpConnector) {
        return WebClient.builder()
                .filter(authorizationToken())
                .clientConnector(clientHttpConnector)
                .build();
    }

    private ExchangeFilterFunction authorizationToken() {
        return (request, next) -> {
            return next.exchange(ClientRequest.from(request).headers((headers) -> {
                headers.setBearerAuth(tokenContext.getAccessToken().getValue());
            }).build());
        };
    }
}
