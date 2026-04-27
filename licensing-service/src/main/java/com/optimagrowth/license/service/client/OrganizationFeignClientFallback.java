package com.optimagrowth.license.service.client;

import com.optimagrowth.license.model.Organization;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OrganizationFeignClientFallback implements OrganizationFeignClient {

    @Override
    public Organization getOrganization(String organizationId) {
        log.warn("Fallback triggered for getOrganization({}). Organization service unreachable", organizationId);

        Organization fallbackOrg = new Organization();
        fallbackOrg.setId(organizationId);
        fallbackOrg.setName("N/A");
        fallbackOrg.setContactName("N/A");
        fallbackOrg.setContactEmail("N/A");
        fallbackOrg.setContactPhone("N/A");

        return fallbackOrg;
    }
}
