package uk.gov.register.configuration;

import org.jvnet.hk2.annotations.Contract;

import java.util.List;
import java.util.Optional;

@Contract
public interface HomepageContentConfiguration {
    Optional<String> getRegisterHistoryPageUrl();
    List<String> getSimilarRegisters();
    List<String> getIndexes();
}
