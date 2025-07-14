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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.qubership.atp.auth.springbootstarter.controller.TestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
@SpringBootTest(classes = TestController.class)
@EnableAutoConfiguration
class SecurityConfigurationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void configure_InvokeRestEndPoint_ReturnStatus200() throws Exception {
        mockMvc.perform(options("/api/test"))
                .andExpect(status().isOk());
    }

    @Test
    void configure_InvokeRestEndPoint_ReturnStatus401() throws Exception {
        int status = 401;
        mockMvc.perform(get("/api/test"))
                .andExpect(status().is(status))
                .andExpect(header().stringValues("Content-Security-Policy","default-src 'self' *"))
                .andExpect(header().stringValues("X-XSS-Protection", "0"));
    }
}