package uk.gov.register.configuration;

import org.jvnet.hk2.annotations.Contract;

@Contract
public interface RegisterDomainConfiguration {
    String getRegisterDomain();
}
