package uk.gov.organisation.client;

import org.jvnet.hk2.annotations.Contract;

import java.net.URI;

@Contract
public interface GovukClientConfiguration {
    URI getGovukEndpoint();
}
