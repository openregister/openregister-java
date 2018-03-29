package uk.gov.register.service;

import com.google.common.collect.Lists;
import uk.gov.register.configuration.ConfigManager;
import uk.gov.register.configuration.RegistersConfiguration;
import uk.gov.register.core.Field;
import uk.gov.register.core.RegisterLinks;
import uk.gov.register.core.RegisterMetadata;
import uk.gov.register.core.RegisterId;

import javax.inject.Inject;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class RegisterLinkService {
    private final ConfigManager configManager;
    private Map<RegisterId, RegisterLinks> registersLinks = new ConcurrentHashMap<>();

    @Inject
    public RegisterLinkService(ConfigManager configManager) {
        this.configManager = configManager;
        this.configManager.getRegistersConfiguration().getAllRegisterMetaData().forEach(m -> updateRegisterLinks(m.getRegisterId()));
    }

    public RegisterLinks getRegisterLinks(RegisterId registerId) {
        return registersLinks.get(registerId);
    }

    public void updateRegisterLinks(RegisterId registerId) {
        RegisterLinks registerLinks = new RegisterLinks(calculateRegistersLinkedFrom(registerId), calculateRegistersLinkedTo(registerId));
        registersLinks.put(registerId, registerLinks);
    }

    private List<String> calculateRegistersLinkedFrom(RegisterId registerId) {
        Collection<Field> allFields = configManager.getFieldsConfiguration().getAllFields();
        Collection<RegisterMetadata> registersMetadata = configManager.getRegistersConfiguration().getAllRegisterMetaData();

        // Get fields that have a register property equal to the name of the current register
        List<String> fieldsMatchingRegister = allFields.stream()
                .filter(f -> f.getRegister().map(r -> r.equals(registerId)).orElse(false))
                .map(f -> f.fieldName)
                .collect(Collectors.toList());

        // Next, get the registers which contain the fields matched above, excluding the current register
        List<String> registers = registersMetadata.stream()
                .filter(m -> m.getFields().stream().anyMatch(f -> fieldsMatchingRegister.contains(f) && !m.getRegisterId().value().equalsIgnoreCase(f)))
                .map(m -> m.getRegisterId().value())
                .collect(Collectors.toList());

        return registers;
    }

    private List<String> calculateRegistersLinkedTo(RegisterId registerId) {
        Collection<Field> allFields = configManager.getFieldsConfiguration().getAllFields();

        RegistersConfiguration registersConfiguration = configManager.getRegistersConfiguration();
        List<String> registerFields = Lists.newArrayList(registersConfiguration.getRegisterMetadata(registerId).getFields());

        return allFields.stream()
                .filter(f -> registerFields.contains(f.fieldName) && f.getRegister().isPresent() && !f.fieldName.equalsIgnoreCase(registerId.value()))
                .map(f -> f.getRegister().get().value())
                .collect(Collectors.toList());
    }
}
