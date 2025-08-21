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

import org.qubership.atp.auth.springbootstarter.exceptions.AtpException;
import org.slf4j.Logger;

import lombok.experimental.UtilityClass;

/**
 * To be deleted in stubbed library, after upgrade to Spring Boot 3.
 */
@UtilityClass
public class ExceptionUtils {

    /**
     * Log and throw the error.
     *
     * @param log logger
     * @param exception error to log and then throw.
     */
    public static void throwWithLog(final Logger log, final AtpException exception) {
        String message = exception.getMessage();
        log.error(message, exception);
        throw exception;
    }
}
