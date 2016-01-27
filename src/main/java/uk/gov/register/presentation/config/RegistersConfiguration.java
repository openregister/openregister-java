package uk.gov.register.presentation.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.dropwizard.jackson.Jackson;
import uk.gov.register.presentation.RegisterData;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;

public class RegistersConfiguration {

    private final List<RegisterData> registers;

    @Inject
    public RegistersConfiguration() throws IOException {
        InputStream registersStream = this.getClass().getClassLoader().getResourceAsStream("config/registers.yaml");
        ObjectMapper yamlObjectMapper = Jackson.newObjectMapper(new YAMLFactory());
        registers = yamlObjectMapper.readValue(registersStream, new TypeReference<List<RegisterData>>() {
        });
    }

    public RegisterData getRegisterData(String registerName) {
        return registers.stream().filter(f -> Objects.equals(f.getRegister().registerName, registerName)).findFirst().get();
    }
}
