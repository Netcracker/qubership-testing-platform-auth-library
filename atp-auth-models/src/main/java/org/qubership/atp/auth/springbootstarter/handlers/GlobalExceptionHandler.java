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

import static org.springframework.http.HttpStatus.valueOf;

import java.net.URL;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.util.Strings;
import org.qubership.atp.auth.springbootstarter.exceptions.AtpException;
import org.qubership.atp.auth.springbootstarter.exceptions.AtpRequestValidationException;
import org.qubership.atp.auth.springbootstarter.feign.exception.FeignClientException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;

@ControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE)
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Include Stack Trace (true/false) configuration variable.
     */
    @Value("${atp.handler.exception.include-stack-trace:false}")
    private boolean includeStackTrace;

    /**
     * FeignClient ObjectMapper bean.
     */
    @Autowired
    private ObjectMapper feignClientObjectMapper;

    /**
     * Global handler for exceptions.
     *
     * @param exception - Exception object
     * @param request   - HttpServletRequest
     * @return ErrorResponse entity
     */
    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<ErrorResponse> commonHandler(Exception exception,
                                                       final HttpServletRequest request) throws Exception {
        if (exception instanceof AccessDeniedException) {
            throw exception;
        }
        if (exception instanceof FeignClientException) {
            return getFeignClientExceptionResponse(exception);
        }
        if (exception instanceof MethodArgumentNotValidException) {
            exception = new AtpRequestValidationException((MethodArgumentNotValidException) exception);
        }
        boolean isAtpException = exception instanceof AtpException;
        if (!isAtpException) {
            log.error("Found internal server error", exception);
            exception = new AtpException();
        }

        ResponseStatus responseStatus =
                AnnotatedElementUtils.findMergedAnnotation(exception.getClass(), ResponseStatus.class);
        HttpStatus status = (responseStatus == null) ? HttpStatus.INTERNAL_SERVER_ERROR : responseStatus.code();
        String reason = (responseStatus == null) ? Strings.EMPTY : responseStatus.reason();
        ErrorResponse error = ErrorResponse.builder()
                .status(status.value())
                .path(request.getServletPath())
                .timestamp(new Date())
                .message(exception.getMessage())
                .reason(reason)
                .build();

        if (includeStackTrace) {
            error.setTrace(Throwables.getStackTraceAsString(exception));
        }
        return ResponseEntity.status(status).body(error);
    }

    private ResponseEntity<ErrorResponse> getFeignClientExceptionResponse(final Exception exception) throws Exception {
        FeignClientException feignException = (FeignClientException) exception;
        String errorMessage = feignException.getErrorMessage();
        JsonNode errorNode = feignClientObjectMapper.readTree(errorMessage);

        Integer status = feignException.getStatus();
        String url = feignException.getRequest().url();
        String path = new URL(url).getPath();
        String message = errorNode.get("message").asText();
        String reason = errorNode.get("reason").asText();

        ErrorResponse error = ErrorResponse.builder()
                .status(status)
                .path(path)
                .timestamp(new Date())
                .message(message)
                .reason(reason)
                .build();

        return ResponseEntity.status(valueOf(status)).body(error);
    }
}
