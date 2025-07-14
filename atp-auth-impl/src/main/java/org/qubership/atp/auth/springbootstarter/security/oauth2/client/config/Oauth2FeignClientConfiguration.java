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

import org.qubership.atp.auth.springbootstarter.config.FeignConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.token.AccessTokenProvider;

import brave.Tracer;


@Configuration
@Profile("default")
public class Oauth2FeignClientConfiguration extends FeignConfiguration {

    @Bean("oauth2FeignClientInterceptor")
    public Oauth2FeignClientInterceptor feignClientInterceptor(
            AccessTokenProvider accessTokenProvider,
            OAuth2ProtectedResourceDetails resourceDetails,
            @Autowired(required = false) Tracer tracer) {
        return new Oauth2FeignClientInterceptor(accessTokenProvider, resourceDetails, tracer);
    }
}
