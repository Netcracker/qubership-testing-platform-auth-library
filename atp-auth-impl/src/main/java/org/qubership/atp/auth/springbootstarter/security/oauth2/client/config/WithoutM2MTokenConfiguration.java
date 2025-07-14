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

import org.qubership.atp.common.logging.interceptor.RestTemplateLogInterceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class WithoutM2MTokenConfiguration {

    /**
     * Without M2M OAuth2 RestTemplate.
     * @return RestTemplate without OAuth2 settings.
     */
    @Bean("m2mRestTemplate")
    @ConditionalOnProperty(
            value = "atp-auth.enable-m2m",
            havingValue = "false",
            matchIfMissing = false
    )
    public RestTemplate m2mRestTemplate(RestTemplateLogInterceptor restTemplateLogInterceptor) {
        RestTemplate restTemplate =
            new RestTemplate(new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory()));
        restTemplate.getInterceptors().add(restTemplateLogInterceptor);
        return restTemplate;
    }
}