package com.optimagrowth.license.service.client;

import com.optimagrowth.license.model.Organization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Component
public class OrganizationDiscoveryClient {
    @Value("${app.services.organiztion-service}")
    private String organizationServiceName;
    @Value("${app.service.organization-api}")
    private String organizationApi;
    @Autowired
    private DiscoveryClient discoveryClient;
    @Autowired @Qualifier("restTemplate")
    RestTemplate restTemplate;

    public Organization getOrganization(String organizationId) {
        if (organizationId == null || organizationId.trim().isEmpty()) {
            throw new IllegalArgumentException("Organization ID must not be null or empty");
        }

        List<ServiceInstance> instances = discoveryClient.getInstances(organizationServiceName);

        if (instances.isEmpty()) {
            throw new IllegalStateException("No instances available for service: " + organizationServiceName);
        }

        ServiceInstance serviceInstance = instances.get(0);
        String serviceUri = String.format(organizationApi, serviceInstance.getUri().toString(), organizationId);

        try {
            ResponseEntity<Organization> response = restTemplate.getForEntity(serviceUri, Organization.class, organizationId);
            return response.getBody();
        } catch (RestClientException e) {
            throw new RestClientException("Failed to fetch organization from " + serviceUri, e);
        }
    }
}