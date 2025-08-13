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

package org.qubership.atp.auth.springbootstarter.feign.exception;

import java.util.Collection;
import java.util.Map;

import feign.Request;
import feign.RetryableException;
import lombok.Getter;

@Getter
@SuppressWarnings("checkstyle:HiddenField")
public class FeignClientException extends RetryableException {

    private static final long serialVersionUID = 958858846136424604L;

    /**
     * Http Status code.
     */
    private final Integer status;

    /**
     * Error message.
     */
    private final String errorMessage;

    /**
     * Http Method.
     */
    private final Request.HttpMethod httpMethod;

    /**
     * Http Request.
     */
    private final Request request;

    /**
     * Http headers map.
     */
    private final Map<String, Collection<String>> headers;

    /**
     * Constructor for {@link FeignClientException}.
     *
     * @param status Http Status code
     * @param errorMessage Error message
     * @param httpMethod Http Method
     * @param headers Http headers map
     * @param request Http Request.
     */
    public FeignClientException(final Integer status,
                                final String errorMessage,
                                final Request.HttpMethod httpMethod,
                                final Map<String, Collection<String>> headers,
                                final Request request) {
        super(status, String.format("%d %s", status, errorMessage), httpMethod, null, request);
        this.status = status;
        this.errorMessage = errorMessage;
        this.headers = headers;
        this.httpMethod = httpMethod;
        this.request = request;
    }

}
