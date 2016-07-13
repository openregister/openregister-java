package uk.gov.register.presentation.config;

import org.jvnet.hk2.annotations.Contract;

@Contract
public interface RegisterDomainConfiguration {
    String getRegisterDomain();
}
