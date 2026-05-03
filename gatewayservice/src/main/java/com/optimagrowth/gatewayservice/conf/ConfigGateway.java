package com.optimagrowth.gatewayservice.conf;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConfigGateway {
    @Bean
    public RouteLocator customLicenseServiceLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("licensing-service", r ->
                     r.path("/license/**")
                      .filters(f ->
                           f.rewritePath("/license/(?<path>.*)", "/${path}"))
                      .uri("lb://licensing-service")
                ).build();

    }
}
