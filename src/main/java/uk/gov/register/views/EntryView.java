package uk.gov.register.views;

import com.fasterxml.jackson.annotation.JsonValue;
import uk.gov.organisation.client.GovukOrganisation;
import uk.gov.register.core.RegisterData;
import uk.gov.register.configuration.PublicBody;
import uk.gov.register.configuration.RegisterDomainConfiguration;
import uk.gov.register.core.Entry;
import uk.gov.register.api.representations.CsvRepresentation;
import uk.gov.register.resources.RequestContext;

import java.util.Optional;

public class EntryView extends CsvRepresentationView<Entry> {
    private Entry entry;

    public EntryView(RequestContext requestContext, PublicBody custodian, Optional<GovukOrganisation.Details> custodianBranding, Entry entry, RegisterDomainConfiguration registerDomainConfiguration, RegisterData registerData) {
        super(requestContext, custodian, custodianBranding, "entry.html", registerDomainConfiguration, registerData);
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
