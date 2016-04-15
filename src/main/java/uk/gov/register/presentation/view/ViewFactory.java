package uk.gov.register.presentation.view;

import org.jvnet.hk2.annotations.Service;
import uk.gov.organisation.client.GovukOrganisation;
import uk.gov.organisation.client.GovukOrganisationClient;
import uk.gov.register.presentation.DbEntry;
import uk.gov.register.presentation.EntryConverter;
import uk.gov.register.presentation.Version;
import uk.gov.register.presentation.config.PublicBodiesConfiguration;
import uk.gov.register.presentation.config.PublicBody;
import uk.gov.register.presentation.config.RegisterDomainConfiguration;
import uk.gov.register.presentation.dao.Entry;
import uk.gov.register.presentation.dao.Item;
import uk.gov.register.presentation.dao.Record;
import uk.gov.register.presentation.resource.Pagination;
import uk.gov.register.presentation.resource.RequestContext;
import uk.gov.register.thymeleaf.ThymeleafView;

import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ViewFactory {
    private final RequestContext requestContext;
    private final EntryConverter entryConverter;
    private final PublicBodiesConfiguration publicBodiesConfiguration;
    private final GovukOrganisationClient organisationClient;
    private final String registerDomain;

    @Inject
    public ViewFactory(RequestContext requestContext,
                       EntryConverter entryConverter,
                       PublicBodiesConfiguration publicBodiesConfiguration,
                       GovukOrganisationClient organisationClient,
                       RegisterDomainConfiguration domainConfiguration) {
        this.requestContext = requestContext;
        this.entryConverter = entryConverter;
        this.publicBodiesConfiguration = publicBodiesConfiguration;
        this.organisationClient = organisationClient;
        this.registerDomain = domainConfiguration.getRegisterDomain();
    }

    public SingleEntryView getSingleEntryView(DbEntry dbEntry) {
        return new SingleEntryView(requestContext, entryConverter.convert(dbEntry), getCustodian(), getBranding());
    }

    public SingleEntryView getLatestEntryView(DbEntry dbEntry) {
        return new SingleEntryView(requestContext, entryConverter.convert(dbEntry), getCustodian(), getBranding(), "latest-entry-of-record.html");
    }

    public EntryListView getRecordsView(List<DbEntry> allDbEntries, Pagination pagination) {
        return new EntryListView(requestContext,
                allDbEntries.stream().map(entryConverter::convert).collect(Collectors.toList()),
                pagination,
                getCustodian(),
                getBranding(),
                "records.html"
        );
    }

    public RecordsView getRecordsView(List<Record> records) {
        return new RecordsView(requestContext, getCustodian(), getBranding(), records);
    }

    public ThymeleafView thymeleafView(String templateName) {
        return new ThymeleafView(requestContext, templateName);
    }

    public BadRequestExceptionView badRequestExceptionView(BadRequestException e) {
        return new BadRequestExceptionView(requestContext, e);
    }

    public HomePageView homePageView(int totalRecords, int totalEntries, Instant lastUpdated) {
        return new HomePageView(getCustodian(), getBranding(), requestContext, totalRecords, totalEntries, lastUpdated, registerDomain);
    }

    public RegisterDetailView registerDetailView(int totalRecords, int totalEntries, int totalItems, Instant lastUpdated) {
        return new RegisterDetailView(getCustodian(), getBranding(), requestContext, entryConverter, totalRecords, totalEntries, totalItems, lastUpdated);
    }

    public ListVersionView listVersionView(List<Version> versions) {
        return new ListVersionView(requestContext, getCustodian(), getBranding(), versions);
    }

    public ItemView getItemView(Item item) {
        return new ItemView(requestContext, getCustodian(), getBranding(), entryConverter, item);
    }

    private PublicBody getCustodian() {
        return publicBodiesConfiguration.getPublicBody(requestContext.getRegister().getRegistry());
    }

    private Optional<GovukOrganisation.Details> getBranding() {
        Optional<GovukOrganisation> organisation = organisationClient.getOrganisation(requestContext.getRegister().getRegistry());
        return organisation.map(GovukOrganisation::getDetails);
    }

    public NewEntryView getNewEntryView(Entry entry) {
        return new NewEntryView(requestContext, getCustodian(), getBranding(), entry);
    }

    public NewEntryListView getNewEntriesView(List<Entry> entries, Pagination pagination) {
        return new NewEntryListView(requestContext, pagination, getCustodian(), getBranding(), entries);
    }

    public RecordView getRecordView(Record record) {
        return new RecordView(requestContext, getCustodian(), getBranding(), entryConverter, record);
    }
}
