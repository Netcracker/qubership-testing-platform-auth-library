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

package org.qubership.atp.auth.springbootstarter.services.client;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.qubership.atp.auth.springbootstarter.entities.ObjectPermissions;
import org.qubership.atp.auth.springbootstarter.entities.Operations;
import org.qubership.atp.auth.springbootstarter.entities.Project;
import org.qubership.atp.auth.springbootstarter.entities.ServiceEntities;
import org.qubership.atp.auth.springbootstarter.entities.UserInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(name = "${feign.atp.users.name}", url = "${feign.atp.users.url}")
public interface UsersFeignClient {

    @RequestMapping(method = RequestMethod.GET,
            value = "${feign.atp.users.route}${atp-auth.project_info_endpoint}/{projectId}")
    Project getUsersByProject(@PathVariable("projectId") UUID projectId);

    @RequestMapping(method = RequestMethod.PATCH, value = "${feign.atp.users.route}/api/v1/users/entities")
    void save(@RequestBody ServiceEntities serviceEntities);

    @RequestMapping(method = RequestMethod.PATCH,
            value = "${feign.atp.users.route}${atp-auth.project_info_endpoint}/{projectId}/services/{serviceName}"
                    + "/objects/{objectId}/permissions")
    ObjectPermissions saveObjectPermissions(@PathVariable UUID projectId,
                                            @PathVariable String serviceName,
                                            @PathVariable String objectId,
                                            @RequestBody Map<UUID, Operations> assignedUsers);

    @RequestMapping(method = RequestMethod.GET,
            value = "${feign.atp.users.route}${atp-auth.project_info_endpoint}/{projectId}/services/{serviceName}"
                    + "/objects/{objectId}/permissions")
    Map<String, Map<UUID, Operations>> getObjectPermissionsByObjectId(@PathVariable UUID projectId,
                                                                      @PathVariable String serviceName,
                                                                      @PathVariable String objectId);

    @RequestMapping(method = RequestMethod.GET,
            value = "${feign.atp.users.route}${atp-auth.project_info_endpoint}/{projectId}/services/{serviceName}"
                    + "/objects/permissions")
    Map<String, Map<UUID, Operations>> getObjectPermissionsByServiceName(@PathVariable UUID projectId,
                                                                         @PathVariable String serviceName);

    @RequestMapping(method = RequestMethod.POST,
            value = "${feign.atp.users.route}${atp-auth.project_info_endpoint}/{projectId}/users/info")
    List<UserInfo> getUsersInfoByProjectId(@PathVariable UUID projectId, @RequestBody List<UUID> uuids);

    @RequestMapping(method = RequestMethod.DELETE,
            value = "${feign.atp.users.route}${atp-auth.project_info_endpoint}/{projectId}/services/{serviceName}"
                    + "/objects/{objectId}/permissions")
    void deleteObjectPermissions(@PathVariable UUID projectId,
                                 @PathVariable String serviceName,
                                 @PathVariable String objectId);

    @RequestMapping(method = RequestMethod.DELETE,
            value = "${feign.atp.users.route}${atp-auth.project_info_endpoint}/{projectId}/services/{serviceName}"
                    + "/objects/permissions")
    void deleteObjectPermissionsBulk(@PathVariable UUID projectId,
                                     @PathVariable String serviceName,
                                     @RequestBody List<String> objectIds);
}
