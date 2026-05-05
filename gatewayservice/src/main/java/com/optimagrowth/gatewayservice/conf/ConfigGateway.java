package com.optimagrowth.gatewayservice.conf;

import com.optimagrowth.gatewayservice.utils.FilterUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import reactor.core.publisher.Mono;

@Configuration
@Slf4j
public class ConfigGateway {

    public final FilterUtils filterUtils;

    @Autowired
    public ConfigGateway(FilterUtils filterUtils) {
        this.filterUtils = filterUtils;
    }

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

    @Bean
    @Order(Integer.MIN_VALUE)
    public GlobalFilter preFilter() {
        return (exchange, chain) -> {
            var headers = exchange.getRequest().getHeaders();

            if (isCorrelationIdPresent(headers)) {
                log.debug("tmx-correlation-id found in tracking filter: {}. ",
                        filterUtils.getCorrelationId(headers));
            } else {
                String correlationID = generateCorrelationId();
                exchange = filterUtils.setCorrelationId(exchange, correlationID);
                log.debug("tmx-correlation-id generated in tracking filter: {}.", correlationID);
            }

            return chain.filter(exchange);
        };
    }

    @Bean
    @Order(Integer.MAX_VALUE)
    public GlobalFilter postFilter() {
        return (exchange, chain) ->
                chain.filter(exchange)
                        .then(Mono.fromRunnable(() -> {
                            var requestHeaders = exchange.getRequest().getHeaders();
                            String correlationId = filterUtils.getCorrelationId(requestHeaders);

                            log.debug("Adding the correlation id to the outbound headers. {}", correlationId);

                            exchange.getResponse().getHeaders().set(FilterUtils.CORRELATION_ID, correlationId);

                            log.debug("Completing outgoing request for {}.", exchange.getRequest().getURI());
                        }));
    }

    private boolean isCorrelationIdPresent(HttpHeaders requestHeaders) {
        if (filterUtils.getCorrelationId(requestHeaders) != null) {
            return true;
        } else {
            return false;
        }
    }

    private String generateCorrelationId() {
        return java.util.UUID.randomUUID().toString();
    }
}
