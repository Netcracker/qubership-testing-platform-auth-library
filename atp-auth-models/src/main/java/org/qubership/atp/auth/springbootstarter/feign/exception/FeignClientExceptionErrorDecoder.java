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

import java.io.IOException;
import java.lang.reflect.Type;

import feign.Response;
import feign.Util;
import feign.codec.DecodeException;
import feign.codec.ErrorDecoder;
import feign.codec.StringDecoder;
import lombok.extern.slf4j.Slf4j;

/**
 * Decodes a failed request response building a FeignClientException.
 * Any error decoding the response body will be thrown as a FeignClientErrorDecodingException
 */
@Slf4j
public class FeignClientExceptionErrorDecoder implements ErrorDecoder {

    /**
     * String decoder object.
     */
    private final StringDecoder stringDecoder = new StringDecoder();

    /**
     * Decode exception from response.
     *
     * @param methodKey String key of a method
     * @param response Response received
     * @return FeignClientException decoded.
     */
    @Override
    public FeignClientException decode(final String methodKey, final Response response) {
        String message = "Null Response Body.";
        try {
            if (response.body() != null) {
                message = decode(response, String.class).toString();
            } else {
                message = stringDecoder.decode(response, String.class).toString();
            }
        } catch (IOException e) {
            log.error("{} Error Deserializing response body from failed feign request response.", methodKey, e);
        }
        return new FeignClientException(response.status(), message, response.request().httpMethod(),
                response.headers(), response.request());
    }

    /**
     * Decode response.
     *
     * @param response Response received
     * @param type Type of body
     * @return decoded exception object.
     */
    private Object decode(final Response response, final Type type) throws IOException {
        Response.Body body = response.body();
        if (String.class.equals(type)) {
            return Util.toString(body.asReader(Util.UTF_8));
        } else {
            throw new DecodeException(response.status(),
                    String.format("%s is not a type supported by this decoder.", type),
                    response.request());
        }
    }
}
