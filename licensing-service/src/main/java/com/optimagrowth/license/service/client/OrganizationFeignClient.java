package com.optimagrowth.license.service.client;

import com.optimagrowth.license.model.Organization;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "organization-service",
        path = "/v1/organization/",
        configuration = OrganizationFeignConfig.class,
        fallback = OrganizationFeignClientFallback.class)
public interface OrganizationFeignClient {
    @GetMapping(value = "{organizationId}")
    Organization getOrganization(@PathVariable("organizationId") String organizationId);
}
