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

package org.qubership.atp.auth.springbootstarter.entities;

public enum Operation {

    /**
     * Operation: create an object.
     */
    CREATE,

    /**
     * Operation: read an object.
     */
    READ,

    /**
     * Operation: update an object.
     */
    UPDATE,

    /**
     * Operation: delete an object.
     */
    DELETE,

    /**
     * Operation: execute an object.
     */
    EXECUTE,

    /**
     * Operation: lock an object.
     */
    LOCK,

    /**
     * Operation: unlock an object.
     */
    UNLOCK;

    @Override
    public String toString() {
        return super.toString().toLowerCase();
    }
}
