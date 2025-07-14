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

import java.util.Set;
import java.util.UUID;

import org.qubership.atp.auth.springbootstarter.holders.DataContextHolder;
import org.qubership.atp.auth.springbootstarter.holders.UserIdContextHolder;
import org.qubership.atp.auth.springbootstarter.holders.UserRolesContextHolder;
import org.qubership.atp.auth.springbootstarter.utils.BearerTokenHttpExtractor;
import org.qubership.atp.auth.springbootstarter.utils.BearerTokenStompExtractor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("default")
public class UtilsConfiguration {

    @Bean("userIdContextHolder")
    public DataContextHolder<UUID> userIdContextHolder() {
        return new UserIdContextHolder();
    }

    @Bean("userRolesContextHolder")
    public DataContextHolder<Set<String>> userRolesContextHolder() {
        return new UserRolesContextHolder();
    }

    @Bean("bearerTokenExtractor")
    public BearerTokenHttpExtractor bearerTokenExtractor() {
        return new BearerTokenHttpExtractor();
    }

    @Bean("bearerTokenStompExtractor")
    public BearerTokenStompExtractor bearerTokenStompExtractor() {
        return new BearerTokenStompExtractor();
    }
}
