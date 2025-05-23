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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.ResourceUtils;

public class SslCertificateProvider implements Provider<List<Certificate>> {

    private static final String CERTIFICATE_PATH = "X.509";

    private List<Certificate> certificateList;

    @Value("${atp-auth.ssl.certificate.dir.path}")
    private String dirPath;

    @Override
    public List<Certificate> get() {
        if (Objects.isNull(certificateList)) {
            certificateList = loadCertificateList();
        }
        return certificateList;
    }

    private List<Certificate> loadCertificateList() {
        try {
            return Stream.of(ResourceUtils.getFile(dirPath).listFiles())
                    .map(this::loadCertificate)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Can not load ssl certificate.", e);
        }
    }

    private Certificate loadCertificate(File file) {
        if (file.isDirectory()) {
            return null;
        }

        try {
            CertificateFactory certificateFactory = CertificateFactory.getInstance(CERTIFICATE_PATH);
            InputStream inputStream = new BufferedInputStream(new FileInputStream(file));
            return certificateFactory.generateCertificate(inputStream);
        } catch (Exception e) {
            throw new RuntimeException("Can not load ssl certificate.", e);
        }
    }
}
