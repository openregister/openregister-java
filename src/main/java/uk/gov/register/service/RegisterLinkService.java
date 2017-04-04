package uk.gov.register.service;

import com.google.common.collect.Lists;
import uk.gov.register.configuration.ConfigManager;
import uk.gov.register.configuration.RegistersConfiguration;
import uk.gov.register.core.Field;
import uk.gov.register.core.RegisterLinks;
import uk.gov.register.core.RegisterMetadata;
import uk.gov.register.core.RegisterName;

import javax.inject.Inject;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class RegisterLinkService {
    private final ConfigManager configManager;
    private Map<RegisterName, RegisterLinks> registersLinks = new ConcurrentHashMap<>();

    @Inject
    public RegisterLinkService(ConfigManager configManager) {
        this.configManager = configManager;
        this.configManager.getRegistersConfiguration().getAllRegisterMetaData().forEach(m -> updateRegisterLinks(m.getRegisterName()));
    }

    public RegisterLinks getRegisterLinks(RegisterName registerName) {
        return registersLinks.get(registerName);
    }

    public void updateRegisterLinks(RegisterName registerName) {
        RegisterLinks registerLinks = new RegisterLinks(calculateRegistersLinkedFrom(registerName), calculateRegistersLinkedTo(registerName));
        registersLinks.put(registerName, registerLinks);
    }

    private List<String> calculateRegistersLinkedFrom(RegisterName registerName) {
        Collection<Field> allFields = configManager.getFieldsConfiguration().getAllFields();
        Collection<RegisterMetadata> registersMetadata = configManager.getRegistersConfiguration().getAllRegisterMetaData();

        // Get fields that have a register property equal to the name of the current register
        List<String> fieldsMatchingRegister = allFields.stream()
                .filter(f -> f.getRegister().map(r -> r.equals(registerName)).orElse(false))
                .map(f -> f.getFieldName())
                .collect(Collectors.toList());

        // Next, get the registers which contain the fields matched above, excluding the current register
        List<String> registers = registersMetadata.stream()
                .filter(m -> m.getFields().stream().anyMatch(f -> fieldsMatchingRegister.contains(f) && !m.getRegisterName().value().equalsIgnoreCase(f)))
                .map(m -> m.getRegisterName().value())
                .collect(Collectors.toList());

        return registers;
    }

    private List<String> calculateRegistersLinkedTo(RegisterName registerName) {
        Collection<Field> allFields = configManager.getFieldsConfiguration().getAllFields();

        RegistersConfiguration registersConfiguration = configManager.getRegistersConfiguration();
        List<String> registerFields = Lists.newArrayList(registersConfiguration.getRegisterMetadata(registerName).getFields());

        return allFields.stream()
                .filter(f -> registerFields.contains(f.getFieldName()) && f.getRegister().isPresent() && !f.getFieldName().equalsIgnoreCase(registerName.value()))
                .map(f -> f.getRegister().get().value())
                .collect(Collectors.toList());
    }
}