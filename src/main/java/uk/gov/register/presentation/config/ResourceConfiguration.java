package uk.gov.register.presentation.config;

import org.jvnet.hk2.annotations.Contract;

@Contract
public interface ResourceConfiguration {
    boolean getEnableDownloadResource();
}
