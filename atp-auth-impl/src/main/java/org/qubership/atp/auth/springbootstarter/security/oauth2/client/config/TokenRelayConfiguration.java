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

import org.qubership.atp.auth.springbootstarter.security.oauth2.client.relay.NoClientOAuth2ProtectedResourceDetails;
import org.qubership.atp.auth.springbootstarter.security.oauth2.client.relay.TokenRelayKeycloakClientContext;
import org.qubership.atp.common.logging.interceptor.RestTemplateLogInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.token.DefaultAccessTokenRequest;
import org.springframework.web.client.RestTemplate;

@Configuration
@Profile("default")
public class TokenRelayConfiguration {

    /**
     * Return {@link OAuth2RestTemplate} which applies a user token to each request.
     */
    @Bean("relayRestTemplate")
    public RestTemplate relayRestTemplate(ClientHttpRequestFactory sslRequestFactory,
                                          RestTemplateLogInterceptor restTemplateLogInterceptor) {
        OAuth2RestTemplate restTemplate = new OAuth2RestTemplate(new NoClientOAuth2ProtectedResourceDetails(),
                new TokenRelayKeycloakClientContext(new DefaultAccessTokenRequest()));
        restTemplate.setRequestFactory(new BufferingClientHttpRequestFactory(sslRequestFactory));
        restTemplate.getInterceptors().add(restTemplateLogInterceptor);

        return restTemplate;
    }
}
