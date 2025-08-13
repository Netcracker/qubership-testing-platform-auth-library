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

package org.qubership.atp.auth.springbootstarter.exceptions;

import static java.lang.String.format;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "ATP-0002")
public class AtpIllegalNullableArgumentException extends AtpException {

    /**
     * Default parametrized message with Reference field name and EntityName parameters.
     */
    public static final String DEFAULT_MESSAGE = "Found illegal nullable %s for the validated %s";

    /**
     * Exception with fixed message.
     *
     * @param message String exception message.
     */
    public AtpIllegalNullableArgumentException(final String message) {
        super(message);
    }

    /**
     * Exception with parametrized message.
     * Example: Found illegal nullable execution request id for the validated Environment Info.
     *
     * @param field Reference field name
     * @param entity Entity name.
     */
    public AtpIllegalNullableArgumentException(final String field, final String entity) {
        super(format(DEFAULT_MESSAGE, field, entity));
    }
}
