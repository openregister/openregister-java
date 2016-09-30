package uk.gov.register.configuration;

import org.jvnet.hk2.annotations.Contract;

import java.util.Optional;

@Contract
public interface RegisterContentPagesConfiguration {
    Optional<String> getRegisterHistoryPageUrl();
}
