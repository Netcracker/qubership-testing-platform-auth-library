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

import org.qubership.atp.auth.springbootstarter.feign.exception.FeignClientExceptionErrorDecoder;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import feign.Logger;
import feign.codec.Encoder;
import feign.codec.ErrorDecoder;

@Configuration
public class FeignConfiguration {

    /**
     * HttpMessageConverters object.
     */
    private volatile HttpMessageConverters feignHttpMessageConverters;

    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }

    /**
     * Create Feign Client Exception Error Decoder bean in case there is no ErrorDecoder bean yet.
     *
     * @return FeignClientExceptionErrorDecoder object.
     */
    @Bean
    @ConditionalOnMissingBean(value = ErrorDecoder.class)
    public FeignClientExceptionErrorDecoder commonFeignErrorDecoder() {
        return new FeignClientExceptionErrorDecoder();
    }

    /**
     * Create {@link Encoder} bean.
     *
     * @param feignClientObjectMapper ObjectMapper bean
     * @return Encoder object.
     */
    @Bean
    @Primary
    public Encoder feignEncoder(@Qualifier("feignClientObjectMapper") ObjectMapper feignClientObjectMapper) {
        HttpMessageConverter jacksonConverter = new MappingJackson2HttpMessageConverter(feignClientObjectMapper);
        ObjectFactory<HttpMessageConverters> objectFactory = () -> {
            if (feignHttpMessageConverters == null) {
                feignHttpMessageConverters = new HttpMessageConverters(jacksonConverter);
            }
            return feignHttpMessageConverters;
        };
        return new SpringEncoder(objectFactory);
    }

    /**
     * Create objectMapper for feign client message converter.
     *
     * @return ObjectMapper bean
     */
    @Bean
    @Qualifier("feignClientObjectMapper")
    public ObjectMapper feignClientObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }
}
