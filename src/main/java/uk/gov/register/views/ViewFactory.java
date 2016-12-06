package uk.gov.register.views;

import org.jvnet.hk2.annotations.Service;
import uk.gov.organisation.client.GovukOrganisation;
import uk.gov.organisation.client.GovukOrganisationClient;
import uk.gov.register.configuration.*;
import uk.gov.register.core.*;
import uk.gov.register.resources.Pagination;
import uk.gov.register.resources.RequestContext;
import uk.gov.register.service.ItemConverter;
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
    private final RegisterData registerData;
    private final RegisterDomainConfiguration registerDomainConfiguration;
    private final RegisterContentPages registerContentPages;
    private final RegisterResolver registerResolver;
    private final RegisterReadOnly register;
    private RegisterTrackingConfiguration registerTrackingConfiguration;

    @Inject
    public ViewFactory(RequestContext requestContext,
                       ItemConverter itemConverter,
                       PublicBodiesConfiguration publicBodiesConfiguration,
                       GovukOrganisationClient organisationClient,
                       RegisterDomainConfiguration registerDomainConfiguration,
                       RegisterContentPagesConfiguration registerContentPagesConfiguration,
                       RegisterData registerData,
                       RegisterTrackingConfiguration registerTrackingConfiguration,
                       RegisterResolver registerResolver,
                       RegisterReadOnly register) {
        this.requestContext = requestContext;
        this.itemConverter = itemConverter;
        this.publicBodiesConfiguration = publicBodiesConfiguration;
        this.organisationClient = organisationClient;
        this.registerDomainConfiguration = registerDomainConfiguration;
        this.registerData = registerData;
        this.registerContentPages = new RegisterContentPages(registerContentPagesConfiguration.getRegisterHistoryPageUrl());
        this.registerTrackingConfiguration = registerTrackingConfiguration;
        this.registerResolver = registerResolver;
        this.register = register;
    }

    public ThymeleafView thymeleafView(String templateName) {
        return new ThymeleafView(requestContext, templateName, registerTrackingConfiguration, registerResolver, register);
    }

    public BadRequestExceptionView badRequestExceptionView(BadRequestException e) {
        return new BadRequestExceptionView(requestContext, e, registerTrackingConfiguration, registerResolver, register);
    }

    public HomePageView homePageView(int totalRecords, int totalEntries, Optional<Instant> lastUpdated) {
        return new HomePageView(
                getRegistry(),
                getBranding(),
                requestContext,
                totalRecords,
                totalEntries,
                lastUpdated,
                registerContentPages,
                registerTrackingConfiguration,
                registerResolver, register);
    }

    public DownloadPageView downloadPageView(Boolean enableDownloadResource) {
        return new DownloadPageView(requestContext, enableDownloadResource, registerTrackingConfiguration, registerResolver, register);
    }

    public RegisterDetailView registerDetailView(int totalRecords, int totalEntries, Optional<Instant> lastUpdated) {
        return new RegisterDetailView(totalRecords, totalEntries, lastUpdated, registerData, registerDomainConfiguration.getRegisterDomain());
    }

    public ItemView getItemView(Item item) {
        return new ItemView(requestContext, getRegistry(), getBranding(), itemConverter, item, registerTrackingConfiguration, registerResolver, register);
    }

    public EntryView getEntryView(Entry entry) {
        return new EntryView(requestContext, getRegistry(), getBranding(), entry, registerTrackingConfiguration, registerResolver, register);
    }

    public EntryListView getEntriesView(Collection<Entry> entries, Pagination pagination) {
        return new EntryListView(requestContext, pagination, getRegistry(), getBranding(), entries, registerTrackingConfiguration, registerResolver, register);
    }

    public EntryListView getRecordEntriesView(String recordKey, Collection<Entry> entries, Pagination pagination) {
        return new EntryListView(requestContext, pagination, getRegistry(), getBranding(), entries, recordKey, registerTrackingConfiguration, registerResolver, register);
    }

    public RecordView getRecordView(Record record) {
        return new RecordView(requestContext, getRegistry(), getBranding(), itemConverter, record, registerTrackingConfiguration, registerResolver, register);
    }

    public RecordListView getRecordListView(List<Record> records, Pagination pagination) {
        return new RecordListView(requestContext, getRegistry(), getBranding(), pagination, itemConverter, records, registerTrackingConfiguration, registerResolver, register);
    }

    private PublicBody getRegistry() {
        return publicBodiesConfiguration.getPublicBody(registerData.getRegister().getRegistry());
    }

    private Optional<GovukOrganisation.Details> getBranding() {
        Optional<GovukOrganisation> organisation = organisationClient.getOrganisation(registerData.getRegister().getRegistry());
        return organisation.map(GovukOrganisation::getDetails);
    }
}
