package uk.gov.register.configuration;

import org.jvnet.hk2.annotations.Contract;

@Contract
public interface ResourceConfiguration {
    boolean getEnableDownloadResource();
}
