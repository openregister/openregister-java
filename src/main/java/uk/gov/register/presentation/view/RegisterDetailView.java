package uk.gov.register.presentation.view;

import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.jena.rdf.model.Model;
import uk.gov.organisation.client.GovukOrganisation;
import uk.gov.register.presentation.RegisterDetail;
import uk.gov.register.presentation.config.PublicBody;
import uk.gov.register.presentation.representations.CsvRepresentation;
import uk.gov.register.presentation.representations.RepresentationView;
import uk.gov.register.presentation.resource.RequestContext;

import java.time.Instant;
import java.util.Optional;

public class RegisterDetailView extends AttributionView implements RepresentationView<RegisterDetail> {
    private final int totalRecords;
    private final int totalEntries;
    private final int totalItems;
    private final Instant lastUpdated;

    public RegisterDetailView(
            PublicBody custodian,
            Optional<GovukOrganisation.Details> custodianBranding,
            RequestContext requestContext,
            int totalRecords,
            int totalEntries,
            int totalItems,
            Instant lastUpdated) {
        super(requestContext, custodian, custodianBranding, "");
        this.totalRecords = totalRecords;
        this.totalEntries = totalEntries;
        this.totalItems = totalItems;
        this.lastUpdated = lastUpdated;
    }

    @JsonValue
    public RegisterDetail getRegisterDetail() {
        return new RegisterDetail(
                getRegisterDomain(),
                totalRecords,
                totalEntries,
                totalItems,
                lastUpdated,
                requestContext.getRegisterData()
        );
    }

    @Override
    public CsvRepresentation<RegisterDetail> csvRepresentation() {
        throw new NotImplementedException("Not implemented");
    }

    @Override
    public Model turtleRepresentation() {
        throw new NotImplementedException("Not implented");
    }
}
