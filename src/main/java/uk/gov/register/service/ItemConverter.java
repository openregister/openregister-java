package uk.gov.register.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Iterables;
import org.jvnet.hk2.annotations.Service;
import uk.gov.register.core.*;
import uk.gov.register.FieldsConfiguration;
import uk.gov.register.configuration.RegisterDomainConfiguration;
import uk.gov.register.resources.RequestContext;

import javax.inject.Inject;
import java.util.Map;

import static uk.gov.register.core.Cardinality.ONE;

@Service
public class ItemConverter {
    private final FieldsConfiguration fieldsConfiguration;
    private final RequestContext requestContext;
    private final String registerDomain;

    @Inject
    public ItemConverter(FieldsConfiguration fieldsConfiguration,
                         RequestContext requestContext,
                         RegisterDomainConfiguration registerDomainConfiguration) {
        this.fieldsConfiguration = fieldsConfiguration;
        this.requestContext = requestContext;
        this.registerDomain = registerDomainConfiguration.getRegisterDomain();
    }

    public FieldValue convert(Map.Entry<String, JsonNode> mapEntry) {
        String fieldName = mapEntry.getKey();
        JsonNode value = mapEntry.getValue();
        FieldConverter fieldConverter = new FieldConverter(fieldsConfiguration.getField(fieldName));
        return fieldConverter.convert(value);
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
                    return new LinkValue.CurieValue(value.textValue(), registerDomain, requestContext.getScheme());
                } 
                return new LinkValue(field.getRegister().get(), registerDomain, requestContext.getScheme(), value.textValue());
            } else if (field.getRegister().isPresent()) {
                return new LinkValue(field.getRegister().get(), registerDomain, requestContext.getScheme(), value.textValue());
                //Note: the equals check below must be replaced with the specified datatype, instead of doing string comparision
                // We should replace this once the datatype register is available
            } else {
                return new StringValue(value.textValue());
            }
        }
    }
}
