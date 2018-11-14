package uk.gov.register.configuration;

import com.fasterxml.jackson.core.type.TypeReference;
import uk.gov.register.core.RegisterMetadata;
import uk.gov.register.core.RegisterId;
import uk.gov.register.util.ResourceJsonFileReader;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

import static java.util.stream.Collectors.toList;

public class RegistersConfiguration {

    private final Collection<RegisterMetadata> registers;

    public RegistersConfiguration(String registersResourceJsonPath) {
        Collection<RegisterConfigRecord> configRecords = new ResourceJsonFileReader()
                .readResourceFromPath(registersResourceJsonPath, new TypeReference<Map<String,RegisterConfigRecord>>(){} );
        registers = configRecords.stream().map(RegisterConfigRecord::getSingleItem).collect(toList());
    }

    public RegisterMetadata getRegisterMetadata(RegisterId registerId) {
        try {
            return registers.stream().filter(f -> Objects.equals(f.getRegisterId(), registerId)).findFirst().get();
        } catch (RuntimeException e) {
            throw new RuntimeException("Cannot get register data for " + registerId, e);
        }
    }

    public Collection<RegisterMetadata> getAllRegisterMetaData() {
        return registers;
    }
}
