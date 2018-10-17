package uk.gov.register.views;

import org.jvnet.hk2.annotations.Service;
import uk.gov.organisation.client.GovukOrganisation;
import uk.gov.organisation.client.GovukOrganisationClient;
import uk.gov.register.configuration.*;
import uk.gov.register.core.*;
import uk.gov.register.exceptions.FieldConversionException;
import uk.gov.register.resources.Pagination;
import uk.gov.register.resources.RequestContext;
import uk.gov.register.service.ItemConverter;

import javax.inject.Inject;
import javax.inject.Provider;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
public class ViewFactory {
    private final RequestContext requestContext;
    private final PublicBodiesConfiguration publicBodiesConfiguration;
    private final GovukOrganisationClient organisationClient;
    private final RegisterDomainConfiguration registerDomainConfiguration;
    private final RegisterResolver registerResolver;
    private final Provider<RegisterReadOnly> register;
    private final Provider<HomepageContentConfiguration> homepageContentConfiguration;
    private final ItemConverter itemConverter;
    private final Provider<RegisterId> registerIdProvider;

    @Inject
    public ViewFactory(final RequestContext requestContext,
                       final PublicBodiesConfiguration publicBodiesConfiguration,
                       final GovukOrganisationClient organisationClient,
                       final RegisterDomainConfiguration registerDomainConfiguration,
                       final Provider<HomepageContentConfiguration> homepageContentConfiguration,
                       final RegisterResolver registerResolver,
                       final Provider<RegisterReadOnly> register,
                       final ItemConverter itemConverter,
                       final Provider<RegisterId> registerIdProvider) {
        this.requestContext = requestContext;
        this.publicBodiesConfiguration = publicBodiesConfiguration;
        this.organisationClient = organisationClient;
        this.registerDomainConfiguration = registerDomainConfiguration;
        this.homepageContentConfiguration = homepageContentConfiguration;
        this.registerResolver = registerResolver;
        this.register = register;
        this.itemConverter = itemConverter;
        this.registerIdProvider = registerIdProvider;
    }

    public ExceptionView exceptionBadRequestView(final String message) {
        return exceptionView("Bad request", message);
    }

    public ExceptionView exceptionNotFoundView() {
        return exceptionView("Page not found", "If you entered a web address please check it was correct.");
    }

    public ExceptionView exceptionFieldConversionView(final String message) {
        return exceptionView("Invalid field value", message);
    }

    public ExceptionView exceptionInconsistencyView(final String message) {
        return exceptionView("Inconsistency in the register data", message);
    }

    public ExceptionView exceptionServerErrorView() {
        return exceptionView("Oops, looks like something went wrong", "500 error");
    }

    public ExceptionView exceptionView(final String heading, final String message) {
        return new ExceptionView(requestContext, heading, message, register.get(), registerResolver);
    }

    public HomePageView homePageView(final int totalRecords, final Optional<Instant> lastUpdated) {
        return new HomePageView(
                getRegistry(),
                getBranding(),
                requestContext,
                totalRecords,
                lastUpdated,
                new HomepageContent(
                        homepageContentConfiguration.get().getIndexes()),
                registerResolver,
                register.get()
        );
    }

    public RegisterDetailView registerDetailView(final int totalRecords, final int totalEntries, final Optional<Instant> lastUpdated, final Optional<String> custodianName) {
        return new RegisterDetailView(totalRecords, totalEntries, lastUpdated, register.get().getRegisterMetadata(),
                registerDomainConfiguration.getRegisterDomain(), custodianName);
    }

    public <T> AttributionView<T> getAttributionView(final String templateName, final T fieldValueMap) {
        return new AttributionView<>(templateName, requestContext, getRegistry(), getBranding(), register.get(), registerResolver, fieldValueMap);
    }

    public AttributionView<ItemView> getItemView(final Item item) throws FieldConversionException {
        return getAttributionView("item.html", getItemMediaView(item));
    }

    public AttributionView<Entry> getEntryView(final Entry entry) {
        return getAttributionView("entry.html", entry);
    }

    public PaginatedView<EntryListView> getEntriesView(final Collection<Entry> entries, final Pagination pagination) {
        return new PaginatedView<>("entries.html", requestContext, getRegistry(), getBranding(), register.get(), registerResolver, pagination, new EntryListView(entries));
    }

    public EntryListView getEntriesView(final Collection<Entry> entries) {
        return new EntryListView(entries);
    }

    public PaginatedView<EntryListView> getRecordEntriesView(final String recordKey, final Collection<Entry> entries, final Pagination pagination) {
        return new PaginatedView<>("entries.html", requestContext, getRegistry(), getBranding(), register.get(), registerResolver, pagination, new EntryListView(entries, recordKey));
    }

    public AttributionView<RecordView> getRecordView(final RecordView record) {
        return getAttributionView("record.html", record);
    }

    public PaginatedView<RecordsView> getRecordsView(final Pagination pagination, final RecordsView recordsView) {
        return new PaginatedView<>("records.html", requestContext, getRegistry(), getBranding(), register.get(), registerResolver, pagination,
                recordsView);
    }

    public ItemView getItemMediaView(final Item item) throws FieldConversionException {
        return new ItemView(item.getSha256hex(), itemConverter.convertItem(item, register.get().getFieldsByName()), getFields());
    }

    public RecordView getRecordMediaView(final Record record) throws FieldConversionException {
        return new RecordView(record, register.get().getFieldsByName(), itemConverter);
    }

    public RecordsView getRecordsMediaView(final List<Record> records) throws FieldConversionException {
        return new RecordsView(records, register.get().getFieldsByName(), itemConverter, false, false);
    }

    public RecordsView getIndexRecordsMediaView(final List<Record> records) throws FieldConversionException {
        return new RecordsView(records, register.get().getFieldsByName(), itemConverter, false, true);
    }

    private PublicBody getRegistry() {
        return publicBodiesConfiguration.getPublicBody(register.get().getRegisterMetadata().getRegistry());
    }

    private Optional<GovukOrganisation.Details> getBranding() {
        final Optional<GovukOrganisation> organisation = organisationClient.getOrganisation(register.get().getRegisterMetadata().getRegistry());
        return organisation.map(GovukOrganisation::getDetails);
    }

    private Collection<Field> getFields() {
        return register.get().getFieldsByName().values();
    }
}
