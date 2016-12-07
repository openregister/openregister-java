package uk.gov.register.configuration;

import com.fasterxml.jackson.core.type.TypeReference;
import uk.gov.register.core.RegisterData;
import uk.gov.register.util.ResourceYamlFileReader;

import javax.inject.Inject;
import java.io.InputStream;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class RegistersConfiguration {

    private final Collection<RegisterData> registers;

    @Inject
    public RegistersConfiguration(Optional<String> registersResourceYamlPath) {
        registers = new ResourceYamlFileReader().readResource(
                registersResourceYamlPath,
                "config/registers.yaml",
                new TypeReference<Map<String, RegisterData>>() {
                }
        );
    }

    public RegistersConfiguration(InputStream registersResourceInputStream) {
        registers = new ResourceYamlFileReader().readResource(
                registersResourceInputStream,
                new TypeReference<Map<String, RegisterData>>() {
                }
        );
    }

    public RegisterData getRegisterData(String registerName) {
        try {
            return registers.stream().filter(f -> Objects.equals(f.getRegister().getRegisterName(), registerName)).findFirst().get();
        } catch (RuntimeException e) {
            throw new RuntimeException("Cannot get register data for " + registerName, e);
        }
    }
}
