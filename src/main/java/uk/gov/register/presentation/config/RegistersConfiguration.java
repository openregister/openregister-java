package uk.gov.register.presentation.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.collect.Lists;
import io.dropwizard.jackson.Jackson;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;

public class RegistersConfiguration {

    private final List<Register> registers;

    @Inject
    public RegistersConfiguration(PublicBodiesConfiguration publicBodiesConfiguration) throws IOException {
        InputStream registersStream = this.getClass().getClassLoader().getResourceAsStream("config/registers.yaml");
        ObjectMapper yamlObjectMapper = makeObjectMapper(publicBodiesConfiguration);
        List<RegisterData> rawRegisters = yamlObjectMapper.readValue(registersStream, new TypeReference<List<RegisterData>>() {});
        registers = Lists.transform(rawRegisters, m -> m.entry);
    }

    private ObjectMapper makeObjectMapper(PublicBodiesConfiguration publicBodiesConfiguration) {
        ObjectMapper yamlObjectMapper = Jackson.newObjectMapper(new YAMLFactory());
        yamlObjectMapper.registerModule(publicBodyModule(publicBodiesConfiguration));
        return yamlObjectMapper;
    }

    private Module publicBodyModule(PublicBodiesConfiguration publicBodiesConfiguration) {
        SimpleModule publicBodyModule = new SimpleModule("PublicBodyModule", new Version(0, 1, 0, "", null, null));
        publicBodyModule.addDeserializer(PublicBody.class, new PublicBodyDeserializer(publicBodiesConfiguration));
        return publicBodyModule;
    }

    public Register getRegister(String registerName) {
        return registers.stream().filter(f -> Objects.equals(f.registerName, registerName)).findFirst().get();
    }

    @JsonIgnoreProperties({"hash", "last-updated"})
    private static class RegisterData{
        @JsonProperty
        Register entry;
    }
}
