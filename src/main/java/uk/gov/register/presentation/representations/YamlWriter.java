package uk.gov.register.presentation.representations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.dropwizard.jackson.Jackson;
import org.jvnet.hk2.annotations.Service;
import uk.gov.register.presentation.EntryView;

import javax.inject.Inject;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

@Produces(ExtraMediaType.TEXT_YAML)
@Service
public class YamlWriter extends RepresentationWriter {
    private final ObjectMapper objectMapper;

    @Inject
    public YamlWriter() {
        objectMapper = Jackson.newObjectMapper(new YAMLFactory());
    }

    @Override
    protected void writeEntriesTo(OutputStream entityStream, List<EntryView> entries) throws IOException, WebApplicationException {
        objectMapper.writeValue(entityStream, entries);
    }

    @Override
    protected void writeEntryTo(OutputStream entityStream, EntryView entry) throws IOException {
        objectMapper.writeValue(entityStream, entry);
    }
}
