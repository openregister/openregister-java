package uk.gov.register.presentation.representations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.dropwizard.jackson.Jackson;
import uk.gov.register.presentation.EntryView;

import javax.inject.Inject;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

@Provider
@Produces(ExtraMediaType.TEXT_YAML)
public class YamlWriter extends RepresentationWriter {
    private final ObjectMapper objectMapper;

    @Inject
    public YamlWriter() {
        objectMapper = Jackson.newObjectMapper(new YAMLFactory());
    }

    @Override
    protected void writeEntriesTo(OutputStream entityStream, Iterable<String> fields, List<EntryView> entries) throws IOException, WebApplicationException {
        objectMapper.writeValue(entityStream, entries);
    }
}
