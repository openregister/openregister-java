package uk.gov.register.configuration;

import org.jvnet.hk2.annotations.Contract;

@Contract
public interface RegisterConfigConfiguration {
    boolean getDownloadConfigs();

    String getExternalConfigDirectory();

    String getFieldsYamlLocation();

    String getRegistersYamlLocation();
}