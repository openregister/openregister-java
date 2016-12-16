package uk.gov.register.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Iterables;
import org.jvnet.hk2.annotations.Service;
import uk.gov.register.configuration.ConfigManager;
import uk.gov.register.core.*;

import javax.inject.Inject;
import java.util.Map;
import java.util.stream.Collectors;

import static uk.gov.register.core.Cardinality.ONE;

@Service
public class ItemConverter {
    private final ConfigManager configManager;

    @Inject
    public ItemConverter(ConfigManager configManager) {
        this.configManager = configManager;
    }

    public FieldValue convert(Map.Entry<String, JsonNode> mapEntry) {
        String fieldName = mapEntry.getKey();
        JsonNode value = mapEntry.getValue();
        FieldConverter fieldConverter = new FieldConverter(configManager.getFieldsConfiguration().getField(fieldName));
        return fieldConverter.convert(value);
    }

    public Map<String, FieldValue> convertItem(Item item) {
        return item.getFieldsStream().collect(Collectors.toMap(Map.Entry::getKey, this::convert));
    }

    private class FieldConverter {
        private final Field field;

        public FieldConverter(Field field) {
            this.field = field;
        }

        public FieldValue convert(JsonNode value) {
            Cardinality cardinality = field.getCardinality();
            if (cardinality == ONE) {
                return convertScalar(value);
            } else {
                return convertArray(value);
            }
        }

        private FieldValue convertArray(JsonNode value) {
            return new ListValue(
                    Iterables.transform(value, this::convertScalar)
            );
        }

        private FieldValue convertScalar(JsonNode value) {
            if (field.getDatatype().getName().equals("curie")) {
                if (value.textValue().contains(":")) {
                    return new LinkValue.CurieValue(value.textValue());
                } 
                return new LinkValue(field.getRegister().get(), value.textValue());
            } else if (field.getRegister().isPresent()) {
                return new LinkValue(field.getRegister().get(), value.textValue());
                //Note: the equals check below must be replaced with the specified datatype, instead of doing string comparision
                // We should replace this once the datatype register is available
            } else {
                return new StringValue(value.textValue());
            }
        }
    }
}
