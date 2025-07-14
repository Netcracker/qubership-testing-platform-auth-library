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

package org.qubership.atp.auth.springbootstarter.ssl;

import java.security.KeyStore;
import java.util.Objects;

import javax.net.ssl.TrustManagerFactory;

import org.springframework.beans.factory.annotation.Autowired;

public class TrustManagerFactoryProvider implements Provider<TrustManagerFactory> {

    @Autowired
    private Provider<KeyStore> keyStoreProvider;

    private TrustManagerFactory trustManagerFactory;

    @Override
    public TrustManagerFactory get() {
        if (Objects.isNull(trustManagerFactory)) {
            trustManagerFactory = createTrustManagerFactory();
        }
        return trustManagerFactory;
    }

    private TrustManagerFactory createTrustManagerFactory() {
        try {
            TrustManagerFactory trustManagerFactory = TrustManagerFactory
                    .getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keyStoreProvider.get());
            return trustManagerFactory;
        } catch (Exception e) {
            throw new RuntimeException("Can not create TrustManagerFactory.", e);
        }
    }
}
