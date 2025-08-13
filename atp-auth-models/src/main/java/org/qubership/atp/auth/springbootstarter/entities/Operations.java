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

import java.io.Serializable;
import java.lang.reflect.Field;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
@AllArgsConstructor
@NoArgsConstructor
public class Operations implements Serializable {
    private static final long serialVersionUID = -6807095207165209809L;
    private boolean create;
    private boolean read;
    private boolean update;
    private boolean delete;
    private boolean execute;
    private boolean lock;
    private boolean unlock;

    /**
     * Return permission for specified operation {@link Operation}.
     */
    public boolean isOperationAvailable(Operation operation) {
        boolean permissionForOperation = false;
        try {
            Field field = this.getClass().getDeclaredField(operation.toString());
            field.setAccessible(true);
            permissionForOperation = (boolean) field.get(this);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            log.error("Failed to get permission for operation \"{}\". {}", operation, e.getMessage());
        }
        return permissionForOperation;
    }
}
