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

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "ATP-0001")
public class AtpEntityNotFoundException extends AtpException {

    /**
     * Default parametrized message with EntityName parameter.
     */
    public static final String DEFAULT_MESSAGE = "Failed to find %s entity";

    /**
     * Default parametrized message with EntityName and Reference field value parameters.
     */
    public static final String DEFAULT_ID_MESSAGE = "Failed to find %s with id: %s";

    /**
     * Default parametrized message with EntityName, Reference field name and Reference field value parameters.
     */
    public static final String DEFAULT_REF_ID_MESSAGE = "Failed to find %s by %s: %s";

    /**
     * Example: Failed to find Environment Info entity.
     *
     * @param entity Entity name.
     */
    public AtpEntityNotFoundException(final String entity) {
        super(format(DEFAULT_MESSAGE, entity));
    }

    /**
     * Example: Failed to find Environment Info entity by id: 123.
     *
     * @param entity Entity name
     * @param id Reference field value.
     */
    public AtpEntityNotFoundException(final String entity, final Object id) {
        super(format(DEFAULT_ID_MESSAGE, entity, id));
    }

    /**
     * Example: Failed to find Environment Info entity by execution request id: 322.
     *
     * @param entity Entity name
     * @param refField Reference field name
     * @param refFieldValue Reference field value.
     */
    public AtpEntityNotFoundException(final String entity, final String refField, final Object refFieldValue) {
        super(format(DEFAULT_REF_ID_MESSAGE, entity, refField, refFieldValue));
    }
}
