package uk.gov.register.presentation.config;

import com.fasterxml.jackson.core.type.TypeReference;
import uk.gov.register.presentation.RegisterData;

import javax.inject.Inject;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class RegistersConfiguration {

    private final List<RegisterData> registers;

    @Inject
    public RegistersConfiguration(Optional<String> registersResourceYamlPath) {
        registers = new ResourceYamlFileReader().readResource(
                registersResourceYamlPath,
                "config/registers.yaml",
                new TypeReference<List<RegisterData>>() {
                },
                registerData -> registerData
        );
    }

    public RegisterData getRegisterData(String registerName) {
        return registers.stream().filter(f -> Objects.equals(f.getRegister().registerName, registerName)).findFirst().get();
    }
}
