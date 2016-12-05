package uk.gov.register.views;

import com.fasterxml.jackson.annotation.JsonValue;
import uk.gov.organisation.client.GovukOrganisation;
import uk.gov.register.configuration.RegisterTrackingConfiguration;
import uk.gov.register.core.PublicBody;
import uk.gov.register.core.Entry;
import uk.gov.register.core.RegisterData;
import uk.gov.register.core.RegisterResolver;
import uk.gov.register.resources.RequestContext;
import uk.gov.register.views.representations.CsvRepresentation;

import java.util.Optional;

public class EntryView extends CsvRepresentationView<Entry> {
    private Entry entry;

    public EntryView(RequestContext requestContext, PublicBody registry, Optional<GovukOrganisation.Details> registryBranding, Entry entry, RegisterData registerData, RegisterTrackingConfiguration registerTrackingConfiguration, RegisterResolver registerResolver) {
        super(requestContext, registry, registryBranding, "entry.html", registerData, registerTrackingConfiguration, registerResolver);
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
}
