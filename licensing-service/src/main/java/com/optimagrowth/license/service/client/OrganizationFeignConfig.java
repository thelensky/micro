package com.optimagrowth.license.service.client;

import feign.Logger;
import org.springframework.context.annotation.Bean;

public class OrganizationFeignConfig {
    @Bean
    public feign.Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL; // Логирует запросы и ответы
    }

    @Bean
    public OrganizationFeignErrorDecoder errorDecoder() {
        return new OrganizationFeignErrorDecoder();
    }
}
