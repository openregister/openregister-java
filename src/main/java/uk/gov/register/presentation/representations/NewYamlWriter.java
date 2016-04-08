package uk.gov.register.presentation.representations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.dropwizard.jackson.Jackson;
import uk.gov.register.presentation.dao.Entry;

import javax.inject.Inject;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.OutputStream;

@Provider
@Produces(ExtraMediaType.TEXT_YAML)
public class NewYamlWriter extends NewRepresentationWriter {
    private final ObjectMapper objectMapper;

    @Inject
    public NewYamlWriter() {
        objectMapper = Jackson.newObjectMapper(new YAMLFactory());
    }

    @Override
    protected void writeEntryTo(OutputStream entityStream, Iterable<String> fields, Entry entry) throws IOException, WebApplicationException {
        objectMapper.writeValue(entityStream, entry);
    }
}
