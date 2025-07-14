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
import java.security.cert.Certificate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;

public class KeyStoreProvider implements Provider<KeyStore> {

    private static final String CERTIFICATE_ALIAS = "certificate";

    @Autowired
    private Provider<List<Certificate>> certificateProvider;

    private KeyStore keyStore;

    @Override
    public KeyStore get() {
        if (Objects.isNull(keyStore)) {
            keyStore = createKeyStore();
        }

        return keyStore;
    }

    private KeyStore createKeyStore() {
        try {
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null, null);
            for (Certificate certificate : certificateProvider.get()) {
                keyStore.setCertificateEntry(CERTIFICATE_ALIAS + UUID.randomUUID().toString(),
                        certificate);
            }
            return keyStore;
        } catch (Exception e) {
            throw new RuntimeException("Can not create key store.", e);
        }
    }
}
