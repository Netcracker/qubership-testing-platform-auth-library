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

package org.qubership.atp.auth.springbootstarter.services;

import java.util.UUID;

import org.qubership.atp.auth.springbootstarter.entities.ServiceEntities;
import org.qubership.atp.auth.springbootstarter.services.client.UsersFeignClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UsersService {

    /**
     * Feign client to Users Service.
     */
    private final UsersFeignClient usersFeignClient;

    /**
     * Kafka Template.
     */
    private final KafkaTemplate<UUID, String> kafkaTemplate;

    /**
     * Topic name to send user service entities.
     */
    @Value("${kafka.service.entities.topic:service_entities}")
    private String topicName;

    /**
     * Service name.
     */
    @Value("${spring.application.name}")
    private String serviceName;

    /**
     * Sends service entities to the atp-users. Stub implementation.
     *
     * @param serviceEntities service entities to send
     */
    public void sendEntities(final ServiceEntities serviceEntities) {
    }

}
