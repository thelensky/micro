package com.optimagrowth.license.service.client;

import feign.Response;
import feign.codec.ErrorDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

@Component
public class OrganizationFeignErrorDecoder implements ErrorDecoder {

    private static final Logger log = LoggerFactory.getLogger(OrganizationFeignErrorDecoder.class);
    private final ErrorDecoder defaultErrorDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        int status = response.status();
        String url = response.request().url();

        log.warn("Feign request failed: {} {} | Status: {}", methodKey, url, status);

        if (status == HttpStatus.NOT_FOUND.value()) {
            return new RuntimeException("Organization not found for URL: " + url);
        } else if (status == HttpStatus.UNAUTHORIZED.value()) {
            return new RuntimeException("Unauthorized access to organization service: " + url);
        } else if (status >= 400 && status < 500) {
            return new HttpClientErrorException(HttpStatus.valueOf(status));
        } else if (status >= 500) {
            log.error("Service error on organization-service: {}", url);
            return new RuntimeException("Internal server error on organization-service");
        }

        return defaultErrorDecoder.decode(methodKey, response);
    }
}