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

import static java.util.Objects.isNull;
import static java.util.stream.Collectors.joining;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import javax.persistence.Id;

import lombok.Data;

@Data
public class UserInfo {

    /**
     * User id.
     */
    @Id
    private UUID id;

    /**
     * User login name.
     */
    private String username;

    /**
     * User first name.
     */
    private String firstName;

    /**
     * User last name.
     */
    private String lastName;

    /**
     * User email.
     */
    private String email;

    /**
     * List of user roles.
     */
    private List<String> roles;

    /**
     * Returns user full name.
     *
     * @return {@link String}.
     */
    public String getFullName() {
        return Stream.of(firstName, lastName)
                .filter(this::isNotEmpty)
                .collect(joining(" "));
    }

    /**
     * Add a new role to roles. If roles is empty, initialize it.
     *
     * @param role to add to roles.
     */
    public void addRole(final String role) {
        if (isNull(this.roles)) {
            this.roles = new ArrayList<>();
        }
        this.roles.add(role);
    }

    private boolean isNotEmpty(final String data) {
        return data != null && data.length() != 0;
    }
}
