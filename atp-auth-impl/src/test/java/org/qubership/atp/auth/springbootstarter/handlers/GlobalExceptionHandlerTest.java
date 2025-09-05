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

package org.qubership.atp.auth.springbootstarter.handlers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.lang.reflect.Method;
import java.util.HashMap;

import javax.persistence.EntityNotFoundException;

import org.junit.Test;
import org.qubership.atp.auth.springbootstarter.exceptions.AtpException;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.MapBindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;

import com.fasterxml.jackson.databind.ObjectMapper;

public class GlobalExceptionHandlerTest {

    /**
     * Global Exception Handler for exceptions processing.
     */
    private final GlobalExceptionHandler globalExceptionHandler = new GlobalExceptionHandler(new ObjectMapper());

    /**
     * Test returning exception with stacktrace.
     *
     * @throws Exception - in case processing exceptions in the GlobalExceptionHandler.
     */
    @Test
    public void testReturnExceptionWithStackTrace() throws Exception {
        ReflectionTestUtils.setField(globalExceptionHandler, "includeStackTrace", true);
        ErrorResponse errorResponse = fillAndCheckResponse();
        assertNotNull(errorResponse.trace);
    }

    /**
     * Test returning exception without stacktrace.
     * @throws Exception - in case processing exceptions in the GlobalExceptionHandler.
     */
    @Test
    public void testReturnExceptionWithoutStackTrace() throws Exception {
        ErrorResponse errorResponse = fillAndCheckResponse();
        assertNull(errorResponse.trace);
    }

    /**
     * Test MethodArgumentNotValidException.
     * @throws Exception - in case processing exceptions in the GlobalExceptionHandler.
     */
    @Test
    public void testHandleMethodArgumentNotValidException() throws Exception {
        BindingResult bindingResult = new MapBindingResult(new HashMap<>(), "objectName");
        bindingResult.addError(new FieldError("objectName", "field1", "Field 1 can not be null"));
        bindingResult.addError(new FieldError("objectName", "field2", "Field 2 can not be null"));
        Method method = this.getClass().getMethod("equals", Object.class);
        MethodParameter parameter = new MethodParameter(method, -1);
        MethodArgumentNotValidException exception =
                new MethodArgumentNotValidException(parameter, bindingResult);
        MockHttpServletRequest request =  new MockHttpServletRequest();
        request.setServletPath("/test/response");

        ResponseEntity<ErrorResponse> responseEntity = globalExceptionHandler.commonHandler(exception, request);

        ErrorResponse errorResponse = responseEntity.getBody();
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertNotNull(errorResponse);
        assertNull(errorResponse.trace);
        assertEquals(HttpStatus.BAD_REQUEST.value(), errorResponse.status);
        assertEquals("/test/response", errorResponse.path);
        assertEquals("Field 1 can not be null, Field 2 can not be null", errorResponse.message);
        assertNotNull(errorResponse.timestamp);
    }

    private ErrorResponse fillAndCheckResponse() throws Exception {
        Exception exception = new EntityNotFoundException("EntityNotFound");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/test/response");
        ResponseEntity<ErrorResponse> responseEntity = globalExceptionHandler.commonHandler(exception, request);
        ErrorResponse errorResponse = responseEntity.getBody();
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertNotNull(errorResponse);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), errorResponse.status);
        assertEquals("/test/response", errorResponse.path);
        assertEquals(AtpException.DEFAULT_MESSAGE, errorResponse.message);
        assertNotNull(errorResponse.timestamp);
        return errorResponse;
    }

}
