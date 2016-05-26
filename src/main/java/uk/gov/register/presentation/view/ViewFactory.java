package uk.gov.register.presentation.view;

import org.jvnet.hk2.annotations.Service;
import uk.gov.organisation.client.GovukOrganisation;
import uk.gov.organisation.client.GovukOrganisationClient;
import uk.gov.register.presentation.ItemConverter;
import uk.gov.register.presentation.config.PublicBodiesConfiguration;
import uk.gov.register.presentation.config.PublicBody;
import uk.gov.register.presentation.config.RegisterDomainConfiguration;
import uk.gov.register.presentation.dao.Entry;
import uk.gov.register.presentation.dao.Item;
import uk.gov.register.presentation.dao.Record;
import uk.gov.register.presentation.resource.IPagination;
import uk.gov.register.presentation.resource.Pagination;
import uk.gov.register.presentation.resource.RequestContext;
import uk.gov.register.thymeleaf.ThymeleafView;

import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
public class ViewFactory {
    private final RequestContext requestContext;
    private final ItemConverter itemConverter;
    private final PublicBodiesConfiguration publicBodiesConfiguration;
    private final GovukOrganisationClient organisationClient;
    private final String registerDomain;

    @Inject
    public ViewFactory(RequestContext requestContext,
                       ItemConverter itemConverter,
                       PublicBodiesConfiguration publicBodiesConfiguration,
                       GovukOrganisationClient organisationClient,
                       RegisterDomainConfiguration domainConfiguration) {
        this.requestContext = requestContext;
        this.itemConverter = itemConverter;
        this.publicBodiesConfiguration = publicBodiesConfiguration;
        this.organisationClient = organisationClient;
        this.registerDomain = domainConfiguration.getRegisterDomain();
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
        return new RegisterDetailView(getCustodian(), getBranding(), requestContext, totalRecords, totalEntries, totalItems, lastUpdated);
    }

    public ItemView getItemView(Item item) {
        return new ItemView(requestContext, getCustodian(), getBranding(), itemConverter, item);
    }

    public EntryView getEntryView(Entry entry) {
        return new EntryView(requestContext, getCustodian(), getBranding(), entry);
    }

    public EntryListView getEntriesView(Collection<Entry> entries, IPagination pagination) {
        return new EntryListView(requestContext, pagination, getCustodian(), getBranding(), entries);
    }

    public RecordView getRecordView(Record record) {
        return new RecordView(requestContext, getCustodian(), getBranding(), itemConverter, record);
    }

    public RecordListView getRecordListView(List<Record> records, Pagination pagination) {
        return new RecordListView(requestContext, getCustodian(), getBranding(), pagination, itemConverter, records);
    }

    private PublicBody getCustodian() {
        return publicBodiesConfiguration.getPublicBody(requestContext.getRegister().getRegistry());
    }

    private Optional<GovukOrganisation.Details> getBranding() {
        Optional<GovukOrganisation> organisation = organisationClient.getOrganisation(requestContext.getRegister().getRegistry());
        return organisation.map(GovukOrganisation::getDetails);
    }
}
