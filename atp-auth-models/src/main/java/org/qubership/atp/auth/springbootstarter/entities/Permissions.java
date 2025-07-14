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
import java.util.Map;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Permissions implements Serializable {
    private static final long serialVersionUID = -8475673651467327135L;
    private Map<String, Operations> leads;
    private Map<String, Operations> qaTaEngineers;
    private Map<String, Operations> devOpsEngineers;
    private Map<String, Operations> atpRunners;
    private Map<String, Operations> atpSupports;

    /**
     * Get permissions for specified group.
     *
     * @param group group for which need to return permissions
     * @return permissions for specified group
     */
    public Map<String, Operations> getPermissionsByGroup(Group group) {
        switch (group) {
            case LEAD:
                return leads;
            case ENGINEER:
                return qaTaEngineers;
            case DEVOPS:
                return devOpsEngineers;
            case EXECUTOR:
                return atpRunners;
            case SUPPORT:
                return atpSupports;
            default:
                return null;
        }
    }

}
