package uk.gov.register;

import com.fasterxml.jackson.core.type.TypeReference;

import javax.inject.Inject;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class RegistersConfiguration {

    private final Collection<Register> registers;

    @Inject
    public RegistersConfiguration(Optional<String> registersResourceYamlPath) {
        registers = new ResourceYamlFileReader().readResource(
                registersResourceYamlPath,
                "config/registers.yaml",
                new TypeReference<Map<String, Register>>() {
                }
        );
    }

    public Register getRegister(String registerName) {
        return registers.stream().filter(f -> Objects.equals(f.registerName, registerName)).findFirst().get();
    }
}
