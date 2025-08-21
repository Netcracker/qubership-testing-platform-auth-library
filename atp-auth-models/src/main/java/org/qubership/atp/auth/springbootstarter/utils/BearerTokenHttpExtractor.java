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

package org.qubership.atp.auth.springbootstarter.utils;

import static org.qubership.atp.auth.springbootstarter.Constants.AUTHORIZATION_HEADER_NAME;
import static org.qubership.atp.auth.springbootstarter.Constants.BEARER_TOKEN_TYPE;

import java.util.Objects;

import jakarta.servlet.http.HttpServletRequest;

public class BearerTokenHttpExtractor implements HeaderValueExtractor<String, HttpServletRequest> {

    /**
     * Extract Bearer token value from Http Authorization Header value.
     *
     * @param request HttpServletRequest object to extract token from its header.
     * @return String token value.
     */
    @Override
    public String extract(final HttpServletRequest request) {
        String token = request.getHeader(AUTHORIZATION_HEADER_NAME);
        if (!Objects.isNull(token)) {
            return token.replace(BEARER_TOKEN_TYPE + " ", "");
        }
        return null;
    }
}
