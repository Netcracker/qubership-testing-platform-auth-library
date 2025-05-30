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

package org.qubership.atp.auth.springbootstarter.security.oauth2.client.config.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.qubership.atp.auth.springbootstarter.config.HttpClientsConfiguration;
import org.qubership.atp.auth.springbootstarter.security.oauth2.client.config.M2MTokenConfiguration;
import org.qubership.atp.auth.springbootstarter.security.oauth2.client.config.Oauth2FeignClientConfiguration;
import org.springframework.context.annotation.Import;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Import({M2MTokenConfiguration.class,
        Oauth2FeignClientConfiguration.class,
        HttpClientsConfiguration.class})
public @interface EnableOauth2FeignClientInterceptor {

}
