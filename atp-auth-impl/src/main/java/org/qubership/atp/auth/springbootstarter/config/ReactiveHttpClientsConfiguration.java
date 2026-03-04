/*
 * # Copyright 2024-2026 NetCracker Technology Corporation
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

package org.qubership.atp.auth.springbootstarter.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import reactor.netty.http.client.HttpClient;

@Configuration
public class ReactiveHttpClientsConfiguration {

    /**
     * HttpConnector for webclient.
     */
    @Bean
    public ClientHttpConnector sslClientHttpConnector(SslContext sslContext) {
        HttpClient httpClient = HttpClient
                .create()
                .secure(x -> x.sslContext(sslContext));

        return new ReactorClientHttpConnector(httpClient);
    }

    /**
     * Ssl context which ignore ssl verification.
     */
    @Bean
    public SslContext reactiveIgnoreVerifySslContext() throws Exception {
        return SslContextBuilder
                .forClient()
                .trustManager(InsecureTrustManagerFactory.INSTANCE)
                .build();
    }
}
