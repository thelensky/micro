package com.optimagrowth.license.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class RestTemplateConfig {
    @Bean
    @Primary
    @LoadBalanced
    public RestTemplate loadBalancedRestTemplate(){
        return new RestTemplate();
    }

    @Bean
    public RestTemplate restTemplate(){
        return new RestTemplate();
    }
}
