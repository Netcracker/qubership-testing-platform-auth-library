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

package org.qubership.atp.auth.springbootstarter.security.interceptors;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.slf4j.MDC;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;


public class MdcHttpRequestInterceptor implements ClientHttpRequestInterceptor {

    List<String> businessIds;

    public MdcHttpRequestInterceptor(List<String> businessIds) {
        this.businessIds = businessIds;
    }

    private String convertIdNameToHeader(String idName) {
        return "X-" + (String) Arrays.stream(idName.split("(?=\\p{Upper})"))
                .map(org.springframework.util.StringUtils::capitalize)
                .collect(Collectors.joining("-"));
    }

    @NotNull
    @Override
    public ClientHttpResponse intercept(@NotNull org.springframework.http.HttpRequest request,
                                        @NotNull byte[] body,
                                        ClientHttpRequestExecution execution) throws IOException {
        businessIds.forEach(idName -> {
            if (MDC.get(idName) != null) {
                request.getHeaders().add(convertIdNameToHeader(idName), MDC.get(idName));
            }
        });
        return execution.execute(request, body);
    }
}
