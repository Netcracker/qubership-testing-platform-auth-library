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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.common.serialization.UUIDSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;

@Configuration
@ConditionalOnProperty(name = "kafka.enable", havingValue = "true")
public class KafkaConfig {

    /**
     * Topic name to send service entities to.
     */
    @Value("${kafka.service.entities.topic}")
    private String kafkaServiceEntitiesTopic;

    /**
     * Number of partitions for the topic.
     */
    @Value("${kafka.service.entities.topic.partitions:1}")
    private int kafkaServiceEntitiesTopicPartitions;

    /**
     * Replication factor for the topic.
     */
    @Value("${kafka.service.entities.topic.replicas:3}")
    private short kafkaServiceEntitiesTopicReplicationFactor;

    /**
     * Kafka bootstrap servers.
     */
    @Value("${spring.kafka.producer.bootstrap-servers}")
    private String kafkaServers;

    /**
     * Create KafkaTemplate for service entities.
     *
     * @return KafkaTemplate of UUID, String; built from mandatory properties.
     */
    @Bean
    public KafkaTemplate<UUID, String> kafkaServiceEntitiesTemplate() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, UUIDSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(props));
    }

    /**
     * Create topic for service entities.
     *
     * @return NewTopic.
     */
    @Bean
    public NewTopic serviceEntitiesTopic() {
        return TopicBuilder.name(kafkaServiceEntitiesTopic)
                .partitions(kafkaServiceEntitiesTopicPartitions)
                .replicas(kafkaServiceEntitiesTopicReplicationFactor)
                .build();
    }

}
