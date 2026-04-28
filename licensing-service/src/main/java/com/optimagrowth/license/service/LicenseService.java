package com.optimagrowth.license.service;

import java.util.*;
import java.util.concurrent.TimeoutException;

import com.optimagrowth.license.service.client.OrganizationClient;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import com.optimagrowth.license.config.ServiceConfig;
import com.optimagrowth.license.model.License;
import com.optimagrowth.license.model.Organization;
import com.optimagrowth.license.repository.LicenseRepository;
import com.optimagrowth.license.service.client.OrganizationDiscoveryClient;
import com.optimagrowth.license.service.client.OrganizationFeignClient;
import com.optimagrowth.license.service.client.OrganizationRestTemplateClient;

@Slf4j
@Service
public class LicenseService {
    MessageSource messages;
    private LicenseRepository licenseRepository;
    ServiceConfig config;
    OrganizationFeignClient organizationFeignClient;
    OrganizationRestTemplateClient organizationRestClient;
    OrganizationDiscoveryClient organizationDiscoveryClient;
    Map<String, OrganizationClient> organizationClientMap;

    @Autowired
    public LicenseService(MessageSource messages,
                          LicenseRepository licenseRepository,
                          ServiceConfig config,
                          OrganizationFeignClient organizationFeignClient,
                          OrganizationRestTemplateClient organizationRestClient,
                          OrganizationDiscoveryClient organizationDiscoveryClient) {
        this.messages = messages;
        this.licenseRepository = licenseRepository;
        this.config = config;
        this.organizationFeignClient = organizationFeignClient;
        this.organizationRestClient = organizationRestClient;
        this.organizationDiscoveryClient = organizationDiscoveryClient;
        organizationClientMap = Map.of(
                "rest", this.organizationRestClient,
                "feign", this.organizationFeignClient,
                "discovery", this.organizationDiscoveryClient
        );
    }

    public License getLicense(String licenseId, String organizationId, String clientType) {
        License license = licenseRepository.findByOrganizationIdAndLicenseId(organizationId, licenseId);
        if (null == license) {
            throw new IllegalArgumentException(String.format(messages.getMessage("license.search.error.message", null, new Locale("ru")), licenseId, organizationId));
        }

        Organization organization = retrieveOrganizationInfo(organizationId, clientType);
        if (null != organization) {
            license.setOrganizationName(organization.getName());
            license.setContactName(organization.getContactName());
            license.setContactEmail(organization.getContactEmail());
            license.setContactPhone(organization.getContactPhone());
        }

        return license.withComment(config.getProperty());
    }

    @CircuitBreaker(name = "organizationService")
    private Organization retrieveOrganizationInfo(String organizationId, String clientType) {
        Organization organization = null;
        String clientTypeLowerCase = clientType.toLowerCase(Locale.ROOT);

        log.debug("I am using the {} client", clientTypeLowerCase);

        if (organizationClientMap.containsKey(clientTypeLowerCase)) {
            organization = organizationClientMap.get(clientTypeLowerCase).getOrganization(organizationId);
        }
        return organization;
    }

    public License createLicense(License license) {
        license.setLicenseId(UUID.randomUUID().toString());
        licenseRepository.save(license);

        return license.withComment(config.getProperty());
    }

    public License updateLicense(License license) {
        licenseRepository.save(license);

        return license.withComment(config.getProperty());
    }

    public String deleteLicense(String licenseId) {
        Optional<License> existing = licenseRepository.findByLicenseId(licenseId);
        existing.ifPresent(license -> licenseRepository.delete(license));
        return String.format(messages.getMessage("license.delete.message", null, null), licenseId);

    }

    @CircuitBreaker(name = "licenseService", fallbackMethod = "fallbackGetLicensesByOrganization")
    @Bulkhead(name = "bulkheadLicenseService", type = Bulkhead.Type.THREADPOOL, fallbackMethod = "fallbackGetLicensesByOrganization")
    @Retry(name = "retryLicenseService", fallbackMethod = "fallbackGetLicensesByOrganization")
    @RateLimiter(name = "rateLimiterLicenseService", fallbackMethod = "fallbackGetLicensesByOrganization")
    public List<License> getLicensesByOrganization(String organizationId) throws TimeoutException {
        randomBreak();
        return licenseRepository.findByOrganizationId(organizationId);
    }

    public List<License> fallbackGetLicensesByOrganization(String organizationId, Throwable throwable) {

        log.warn("Service crashed with error: {}", throwable.getMessage());
        return Collections.emptyList();
    }

    private void randomBreak() throws TimeoutException {
        if (new Random().nextInt(3) == 0) {
            try {
                Thread.sleep(100);
                throw new TimeoutException("Simulate timeout");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Thread interrupt during sleep", e);
            }
        }
    }
}