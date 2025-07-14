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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.qubership.atp.auth.springbootstarter.security.interceptors.MdcHttpRequestInterceptor;
import org.qubership.atp.common.logging.interceptor.RestTemplateLogInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.token.AccessTokenProvider;
import org.springframework.security.oauth2.client.token.AccessTokenProviderChain;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsAccessTokenProvider;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsResourceDetails;
import org.springframework.web.client.RestTemplate;

@Configuration
@ConditionalOnProperty(
        value = "atp-auth.enable-m2m",
        matchIfMissing = true
)
public class M2MTokenConfiguration {

    @Value("${keycloak.auth-server-url}")
    private String baseUrl;
    @Value("${keycloak.realm}")
    private String realm;
    @Value("${keycloak.resource}")
    private String clientId;
    @Value("${keycloak.credentials.secret}")
    private String clientSecret;
    @Value("${atp.logging.business.keys:userId,projectId,executionRequestId,testRunId,bvTestRunId,bvTestCaseId,"
            + "environmentId,systemId,subscriberId,tsgSessionId,svpSessionId,dataSetId,dataSetListId,attributeId,"
            + "itfLiteRequestId,reportType,itfSessionId,itfContextId,callChainId}")
    private String businessIds;

    /**
     * Resource details for m2m authentication.
     */
    @Bean("resourceDetails")
    public OAuth2ProtectedResourceDetails resourceDetails() {
        String issuer = baseUrl + "/realms/" + realm;
        String tokenUrl = issuer + "/protocol/openid-connect/token";

        ClientCredentialsResourceDetails resource = new ClientCredentialsResourceDetails();
        resource.setAccessTokenUri(tokenUrl);
        resource.setClientId(clientId);
        resource.setClientSecret(clientSecret);

        return resource;
    }

    /**
     * M2M OAuth2RestTemplate.
     */
    @Bean("m2mRestTemplate")
    public RestTemplate m2mRestTemplate(OAuth2ProtectedResourceDetails resourceDetails,
                                        AccessTokenProvider accessTokenProvider,
                                        ClientHttpRequestFactory sslRequestFactory,
                                        RestTemplateLogInterceptor restTemplateLogInterceptor) {
        OAuth2RestTemplate restTemplate = new OAuth2RestTemplate(resourceDetails);
        restTemplate.setAccessTokenProvider(accessTokenProvider);
        restTemplate.setRequestFactory(new BufferingClientHttpRequestFactory(sslRequestFactory));
        restTemplate.getInterceptors().add(restTemplateLogInterceptor);
        restTemplate.getInterceptors().add(new MdcHttpRequestInterceptor(getBusinessIdList(businessIds)));
        return restTemplate;
    }

    private List<String> getBusinessIdList(String businessIds) {
        return StringUtils.isNotBlank(businessIds)
                ? Arrays.stream(businessIds.split(","))
                .map(String::trim).collect(Collectors.toList())
                : Collections.emptyList();
    }

    /**
     * AccessTokenProvider with ignore ssl certificate verification.
     */
    @Bean
    public AccessTokenProvider accessTokenProvider(ClientHttpRequestFactory sslRequestFactory) {
        ClientCredentialsAccessTokenProvider tokenProvider = new ClientCredentialsAccessTokenProvider();
        tokenProvider.setRequestFactory(sslRequestFactory);

        return new AccessTokenProviderChain(Collections.singletonList(tokenProvider));
    }
}
