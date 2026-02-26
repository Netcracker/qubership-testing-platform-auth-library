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

package org.qubership.atp.auth.springbootstarter.config;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

@Configuration
public class HttpClientsConfiguration {

    /**
     * Http client with trusted ssl certificate.
     *
     * @param sslContext SSLContext object
     * @return HttpClient object created and configured.
     */
    @Bean
    public HttpClient sslHttpClient(final SSLContext sslContext) {
        return HttpClients.custom()
                .setSSLSocketFactory(new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE))
                .build();
    }

    /**
     * RequestFactory with ignore ssl certificate verification.
     *
     * @param sslHttpClient HttpClient object
     * @return ClientHttpRequestFactory factory created and configured.
     */
    @Bean
    public ClientHttpRequestFactory sslRequestFactory(final HttpClient sslHttpClient) {
        HttpComponentsClientHttpRequestFactory sslRequestFactory =
                new HttpComponentsClientHttpRequestFactory();

        sslRequestFactory.setHttpClient(sslHttpClient);
        return sslRequestFactory;
    }

    /**
     * Ssl context which ignore ssl verification.
     *
     * @return SSLContext object created and configured.
     */
    @Bean
    public SSLContext ignoreVerifySslContext()
            throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        return SSLContexts.custom()
                .loadTrustMaterial(null, (x, y) -> true)
                .build();
    }

}
