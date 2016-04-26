package uk.gov.register.presentation.view;

import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import uk.gov.organisation.client.GovukOrganisation;
import uk.gov.register.presentation.config.PublicBody;
import uk.gov.register.presentation.dao.Entry;
import uk.gov.register.presentation.representations.CsvRepresentation;
import uk.gov.register.presentation.representations.RepresentationView;
import uk.gov.register.presentation.resource.RequestContext;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.Optional;

public class NewEntryView extends AttributionView implements RepresentationView<Entry> {
    private static final String SPEC_PREFIX = "https://openregister.github.io/specification/#";

    private Entry entry;

    public NewEntryView(RequestContext requestContext, PublicBody custodian, Optional<GovukOrganisation.Details> custodianBranding, Entry entry) {
        super(requestContext, custodian, custodianBranding, "new-entry.html");
        this.entry = entry;
    }

    @JsonValue
    public Entry getEntry() {
        return entry;
    }

    @Override
    public CsvRepresentation<Entry> csvRepresentation() {
        return new CsvRepresentation<>(Entry.csvSchema(), getEntry());
    }

    @Override
    public Model turtleRepresentation() {
        Model model = ModelFactory.createDefaultModel();
        Property entryNumberProperty = model.createProperty(SPEC_PREFIX + "entry-number-field");
        Property entryTimestampProperty = model.createProperty(SPEC_PREFIX + "entry-timestamp-field");
        Property itemProperty = model.createProperty(SPEC_PREFIX + "item-resource");

        model.createResource(entryUri().toString())
                .addProperty(entryNumberProperty, entry.entryNumber)
                .addProperty(entryTimestampProperty, entry.getTimestamp())
                .addProperty(itemProperty, itemUri().toString());

        model.setNsPrefix("register-metadata", SPEC_PREFIX);
        return model;
    }

    public URI entryUri() {
        return UriBuilder.fromUri(getHttpServletRequest().getRequestURI()).replacePath(null).path("entry").path(entry.entryNumber).build();
    }

    private URI itemUri() {
        return UriBuilder.fromUri(getHttpServletRequest().getRequestURI()).replacePath(null).path("item").path(entry.getSha256hex()).build();
    }
}
