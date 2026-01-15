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

package org.qubership.atp.auth.springbootstarter.config;

import java.util.UUID;

import org.qubership.atp.auth.springbootstarter.services.UsersService;
import org.qubership.atp.auth.springbootstarter.services.client.UsersFeignClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;

import lombok.AllArgsConstructor;

@Configuration
@AllArgsConstructor
@EnableFeignClients("org.qubership.atp.auth.springbootstarter.services.client")
public class ServiceConfiguration {

    private final UsersFeignClient usersFeignClient;

    @Bean
    @ConditionalOnProperty(name = "kafka.enable", havingValue = "false", matchIfMissing = true)
    public UsersService usersService() {
        return new UsersService(usersFeignClient, null);
    }

    @Bean
    @ConditionalOnProperty(name = "kafka.enable", havingValue = "true")
    public UsersService usersServiceWithKafka(KafkaTemplate<UUID, String> kafkaServiceEntitiesTemplate) {
        return new UsersService(usersFeignClient, kafkaServiceEntitiesTemplate);
    }

}
