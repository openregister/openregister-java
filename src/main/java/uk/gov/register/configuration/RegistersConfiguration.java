package uk.gov.register.configuration;

import com.fasterxml.jackson.core.type.TypeReference;
import uk.gov.register.core.RegisterMetadata;
import uk.gov.register.core.RegisterName;
import uk.gov.register.util.ResourceYamlFileReader;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class RegistersConfiguration {

    private final Collection<RegisterMetadata> registers;

    public RegistersConfiguration(Optional<String> registersResourceYamlPath) {
        registers = new ResourceYamlFileReader().readResource(
                registersResourceYamlPath,
                "config/registers.yaml",
                new TypeReference<Map<String, RegisterMetadata>>() {
                }
        );
    }

    public RegistersConfiguration(byte[] registersConfig) {
        registers = new ResourceYamlFileReader().readResource(
                registersConfig,
                new TypeReference<Map<String, RegisterMetadata>>() {
                }
        );
    }

    public RegisterMetadata getRegisterMetadata(RegisterName registerName) {
        try {
            return registers.stream().filter(f -> Objects.equals(f.getRegisterName(), registerName)).findFirst().get();
        } catch (RuntimeException e) {
            throw new RuntimeException("Cannot get register data for " + registerName, e);
        }
    }
}
