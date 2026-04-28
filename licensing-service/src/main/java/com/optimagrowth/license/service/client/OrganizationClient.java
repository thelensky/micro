package com.optimagrowth.license.service.client;

import com.optimagrowth.license.model.Organization;

public interface OrganizationClient {
    Organization getOrganization(String organizationId);
}
