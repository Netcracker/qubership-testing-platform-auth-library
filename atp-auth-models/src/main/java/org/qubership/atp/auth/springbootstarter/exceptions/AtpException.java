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

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR, reason = "ATP-0000")
public class AtpException extends RuntimeException {

    /**
     * Default fixed exception message.
     */
    public static final String DEFAULT_MESSAGE = "Internal Server Error. "
            + "Please contact the administrator for assistance";

    /**
     * Default constructor.
     */
    public AtpException() {
        super(DEFAULT_MESSAGE);
    }

    /**
     * Constructor from String exception message.
     *
     * @param message String exception message.
     */
    public AtpException(final String message) {
        super(message);
    }
}
