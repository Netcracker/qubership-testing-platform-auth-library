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

import java.security.KeyStore;
import java.security.cert.Certificate;
import java.util.List;

import javax.net.ssl.TrustManagerFactory;

import org.qubership.atp.auth.springbootstarter.ssl.KeyStoreProvider;
import org.qubership.atp.auth.springbootstarter.ssl.Provider;
import org.qubership.atp.auth.springbootstarter.ssl.SslCertificateProvider;
import org.qubership.atp.auth.springbootstarter.ssl.TrustManagerFactoryProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(
        value = "atp-auth.ssl.certificate.verify",
        matchIfMissing = false
)
public class SslProviderConfiguration {

    /**
     * KeyStore provider.
     */
    @Bean
    public Provider<KeyStore> keyStoreProvider() {
        return new KeyStoreProvider();
    }

    /**
     * Ssl certificates provider.
     */
    @Bean
    public Provider<List<Certificate>> certificatesProvider() {
        return new SslCertificateProvider();
    }

    /**
     * Trust manager factory provider.
     */
    @Bean
    public Provider<TrustManagerFactory> trustManagerFactoryProvider() {
        return new TrustManagerFactoryProvider();
    }
}
