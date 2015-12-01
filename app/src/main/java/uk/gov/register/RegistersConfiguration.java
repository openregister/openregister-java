package uk.gov.register;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.collect.Lists;
import io.dropwizard.jackson.Jackson;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;

public class RegistersConfiguration {

    private final List<Register> registers;

    public RegistersConfiguration() {
        try {
            InputStream registersStream = this.getClass().getClassLoader().getResourceAsStream("config/registers.yaml");
            ObjectMapper yamlObjectMapper = Jackson.newObjectMapper(new YAMLFactory());
            List<RegisterData> rawRegisters = yamlObjectMapper.readValue(registersStream, new TypeReference<List<RegisterData>>() {
            });
            registers = Lists.transform(rawRegisters, m -> m.entry);
        } catch (IOException e) {
            throw new RuntimeException("Error loading registers configuration.", e);
        }
    }

    public Register getRegister(String registerName) {
        return registers.stream().filter(f -> Objects.equals(f.registerName, registerName)).findFirst().get();
    }

    @JsonIgnoreProperties({"hash", "last-updated"})
    private static class RegisterData {
        @JsonProperty
        Register entry;
    }
}
