package uk.gov.register.configuration;

import com.fasterxml.jackson.core.type.TypeReference;
import uk.gov.register.core.RegisterMetadata;
import uk.gov.register.core.RegisterName;
import uk.gov.register.util.ResourceYamlFileReader;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

public class RegistersConfiguration {

    private final Collection<RegisterMetadata> registers;

    public RegistersConfiguration(String registersResourceYamlPath) throws IOException {
        registers = new ResourceYamlFileReader().readResourceFromPath(registersResourceYamlPath, new TypeReference<Map<String,
                RegisterMetadata>>() {
        });
    }

    public RegisterMetadata getRegisterMetadata(RegisterName registerName) {
        try {
            return registers.stream().filter(f -> Objects.equals(f.getRegisterName(), registerName)).findFirst().get();
        } catch (RuntimeException e) {
            throw new RuntimeException("Cannot get register data for " + registerName, e);
        }
    }

    public Collection<RegisterMetadata> getAllRegisterMetaData() {
        return registers;
    }
}
