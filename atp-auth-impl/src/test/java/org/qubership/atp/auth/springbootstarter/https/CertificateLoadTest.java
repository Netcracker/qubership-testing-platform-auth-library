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

package org.qubership.atp.auth.springbootstarter.https;

import java.security.KeyStore;
import java.security.cert.Certificate;
import java.util.List;

import javax.net.ssl.TrustManagerFactory;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.qubership.atp.auth.springbootstarter.config.TestConfiguration;
import org.qubership.atp.auth.springbootstarter.ssl.Provider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = TestConfiguration.class, properties =
        {"atp-auth.ssl.certificate.verify=true",
                "atp-auth.ssl.certificate.dir.path=classpath:ssl"})
public class CertificateLoadTest {

    @Autowired
    private Provider<List<Certificate>> certificateProvider;
    @Autowired
    private Provider<KeyStore> keyStoreProvider;
    @Autowired
    private Provider<TrustManagerFactory> trustManagerFactoryProvider;

    @Test
    public void testCertificateLoading() {
        List<Certificate> certificates = certificateProvider.get();
        Assert.assertNotNull(certificates);
    }

    @Test
    public void testKeyStoreLoading() {
        KeyStore keyStore = keyStoreProvider.get();
        Assert.assertNotNull(keyStore);
    }

    @Test
    public void testTrustManagerFactoryLoading() {
        TrustManagerFactory trustManagerFactory = trustManagerFactoryProvider.get();
        Assert.assertNotNull(trustManagerFactory);
    }
}
