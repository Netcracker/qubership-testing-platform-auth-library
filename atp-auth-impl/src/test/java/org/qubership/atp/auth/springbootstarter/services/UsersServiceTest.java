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

import java.util.Arrays;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.qubership.atp.auth.springbootstarter.entities.ServiceEntities;
import org.qubership.atp.auth.springbootstarter.services.client.UsersFeignClient;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import com.fasterxml.jackson.core.JsonProcessingException;

@RunWith(MockitoJUnitRunner.class)
public class UsersServiceTest {

    @Mock
    KafkaTemplate<UUID, String> kafkaTemplate;
    @Mock
    UsersFeignClient usersFeignClient;
    @InjectMocks
    private UsersService usersService;

    @Test
    public void SendEntitiesTest() throws JsonProcessingException {
        ServiceEntities entities = new ServiceEntities();
        entities.setUuid(UUID.randomUUID());
        entities.setService("test-service");
        entities.setEntities(Arrays.asList("test1", "test2", "test3"));

        usersService.sendEntities(entities);
        Mockito.verify(kafkaTemplate).send(Mockito.any(), Mockito.any());

        ReflectionTestUtils.setField(usersService, "kafkaTemplate", null);
        usersService.sendEntities(entities);
        Mockito.verify(usersFeignClient).save(entities);
    }
}
