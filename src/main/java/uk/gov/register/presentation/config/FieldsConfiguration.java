package uk.gov.register.presentation.config;

import org.jvnet.hk2.annotations.Contract;

import java.util.Map;

@Contract
public interface FieldsConfiguration {
    Map<String,FieldConfiguration> getFields();
}
