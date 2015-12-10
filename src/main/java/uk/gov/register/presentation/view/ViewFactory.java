package uk.gov.register.presentation.view;

import org.jvnet.hk2.annotations.Service;
import uk.gov.register.presentation.DbEntry;
import uk.gov.register.presentation.EntryConverter;
import uk.gov.register.presentation.Version;
import uk.gov.register.presentation.config.PublicBodiesConfiguration;
import uk.gov.register.presentation.config.PublicBody;
import uk.gov.register.presentation.resource.Pagination;
import uk.gov.register.presentation.resource.RequestContext;
import uk.gov.register.thymeleaf.BadRequestExceptionView;
import uk.gov.register.thymeleaf.HomePageView;
import uk.gov.register.thymeleaf.ThymeleafView;

import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.client.Client;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ViewFactory {
    private final RequestContext requestContext;
    private final EntryConverter entryConverter;
    private final PublicBodiesConfiguration publicBodiesConfiguration;
    private final Client client;

    @Inject
    public ViewFactory(RequestContext requestContext, EntryConverter entryConverter, PublicBodiesConfiguration publicBodiesConfiguration, Client client) {
        this.requestContext = requestContext;
        this.entryConverter = entryConverter;
        this.publicBodiesConfiguration = publicBodiesConfiguration;
        this.client = client;
    }

    public SingleEntryView getSingleEntryView(DbEntry dbEntry) {
        return new SingleEntryView(requestContext, entryConverter.convert(dbEntry), getCustodian(), getBranding());
    }

    public SingleEntryView getLatestEntryView(DbEntry dbEntry) {
        return new SingleEntryView(requestContext, entryConverter.convert(dbEntry), getCustodian(), getBranding(), "latest-entry-of-record.html");
    }

    public EntryListView getEntriesView(List<DbEntry> allDbEntries, Pagination pagination) {
        return new EntryListView(requestContext,
                allDbEntries.stream().map(entryConverter::convert).collect(Collectors.toList()),
                pagination,
                getCustodian(),
                getBranding(),
                "entries.html"
        );
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

    public ThymeleafView thymeleafView(String templateName) {
        return new ThymeleafView(requestContext, templateName);
    }

    public BadRequestExceptionView badRequestExceptionView(BadRequestException e) {
        return new BadRequestExceptionView(requestContext, e);
    }

    public HomePageView homePageView(int totalRecords, LocalDateTime lastUpdated) {
        return new HomePageView(getCustodian(), getBranding(), requestContext, totalRecords, lastUpdated);
    }

    public ListVersionView listVersionView(List<Version> versions) throws Exception {
        return new ListVersionView(requestContext, getCustodian(), getBranding(), versions);
    }

    private PublicBody getCustodian() {
        return publicBodiesConfiguration.getPublicBody(requestContext.getRegister().getRegistry());
    }

    private GovukOrganisation.Details getBranding() {
        String custodianId = requestContext.getRegister().getRegistry();
        return client.target("https://www.gov.uk/api/organisations/" + custodianId)
                .request()
                .get(GovukOrganisation.class)
                .getDetails();
    }
}
