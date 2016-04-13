package uk.gov.register.presentation.view;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import uk.gov.organisation.client.GovukOrganisation;
import uk.gov.register.presentation.config.PublicBody;
import uk.gov.register.presentation.dao.Entry;
import uk.gov.register.presentation.representations.RepresentationView;
import uk.gov.register.presentation.resource.Pagination;
import uk.gov.register.presentation.resource.RequestContext;

import java.util.List;
import java.util.Optional;

public class NewEntryListView extends AttributionView implements RepresentationView {
    private Pagination pagination;
    private List<Entry> entries;

    public NewEntryListView(RequestContext requestContext, Pagination pagination, PublicBody custodian, Optional<GovukOrganisation.Details> custodianBranding, List<Entry> entries) {
        super(requestContext, custodian, custodianBranding, "new-entries.html");
        this.pagination = pagination;
        this.entries = entries;
    }

    @JsonValue
    public List<Entry> getEntries() {
        return entries;
    }

    @SuppressWarnings("unused, used from templates")
    public Pagination getPagination() {
        return pagination;
    }

    @Override
    public CsvSchema csvSchema() {
        CsvMapper csvMapper = new CsvMapper();
        csvMapper.disable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY);
        return csvMapper.schemaFor(Entry.class);
    }
}
