package com.optimagrowth.license.service.client;

import com.optimagrowth.license.model.Organization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@Component
public class OrganizationRestTemplateClient {

    @Value("${app.service.organization-url}")
    private String organizationUrl;
    @Autowired
    RestTemplate loadBalancedRestTemplate;
    private static final Logger log = LoggerFactory.getLogger(OrganizationRestTemplateClient.class);


    public Organization getOrganization(String organizationId) {

        if (organizationId == null || organizationId.trim().isEmpty()) {
            throw new IllegalArgumentException("Organization ID must not be null or empty");
        }

        try {
            return loadBalancedRestTemplate.exchange(organizationUrl, HttpMethod.GET, null, Organization.class, organizationId).getBody();
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Organization not found: {}", organizationId);
            return null;
        } catch (ResourceAccessException e) {
            log.error("Service unavailable for organizationId {}", organizationId);
            throw new RuntimeException("Organization service is unreachable", e);
        } catch (Exception e) {
            log.error("Unexpected error fetching organization: {}", organizationId);
            throw e;
        }


    }
}
