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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import feign.Request;
import feign.Response;

public class FeignClientExceptionErrorDecoderClass {

    /**
     * FeignClientExceptionErrorDecoder object.
     */
    private FeignClientExceptionErrorDecoder feignClientExceptionErrorDecoder;

    /**
     * Before-tests actions.
     */
    @Before
    public void setUp() {
        feignClientExceptionErrorDecoder = new FeignClientExceptionErrorDecoder();
    }

    /**
     * Test of decoding of null body.
     */
    @Test
    public void decode_WhenBodyNotNull_ShouldReturnFeignClientException() {
        String methodKey = "";
        String bodyString = "{\"status\":404,"
                + "\"path\":\"/catalog/api/v1/projects/c9645d45-466e-412f-9082-5148023c9689\""
                + ",\"timestamp\":\"2024-05-20T13:35:42.441+0000\",\"trace\":null,"
                + "\"message\":\"Failed to find Project with id: c9645d45-466e-412f-9082-5148023c9689\","
                + "\"reason\":\"ATP-0001\",\"details\":null}";
        Response response = mock(Response.class);
        Response.Body body = new Response.Body() {
            @Override
            public void close() {
            }

            @Override
            public Integer length() {
                return null;
            }

            @Override
            public boolean isRepeatable() {
                return false;
            }

            @Override
            public InputStream asInputStream() {
                return new ByteArrayInputStream(bodyString.getBytes());
            }

            @Override
            public Reader asReader(final Charset charset) throws IOException {
                return new InputStreamReader(asInputStream(), charset);
            }
        };

        Integer code = HttpStatus.SC_NOT_FOUND;

        when(response.body()).thenReturn(body);
        when(response.status()).thenReturn(code);

        Request request = mock(Request.class);
        when(request.httpMethod()).thenReturn(Request.HttpMethod.GET);
        when(response.request()).thenReturn(request);
        Map<String, Collection<String>> headers = new HashMap<>();
        when(response.headers()).thenReturn(headers);

        FeignClientException actual = feignClientExceptionErrorDecoder.decode(methodKey, response);
        Assert.assertEquals(bodyString, actual.getErrorMessage());
        Assert.assertEquals(code, actual.getStatus());
    }
}
