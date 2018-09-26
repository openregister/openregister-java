package uk.gov.organisation.client;

import com.google.common.base.Throwables;
import io.dropwizard.testing.junit.DropwizardClientRule;
import io.dropwizard.util.Duration;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Optional;
import java.util.function.Supplier;

import static java.util.Optional.empty;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.organisation.client.TestJerseyClientBuilder.createTestJerseyClient;

public class GovukOrganisationClientTest {
    // pulled from https://www.gov.uk/api/organisations/department-for-education
    public static final String DEPARTMENT_FOR_EDUCATION_JSON = "{\"id\":\"https://www.gov.uk/api/organisations/department-for-education\",\"title\":\"Department for Education\",\"format\":\"Ministerial department\",\"updated_at\":\"2015-06-03T14:12:50.000+01:00\",\"web_url\":\"https://www.gov.uk/government/organisations/department-for-education\",\"details\":{\"slug\":\"department-for-education\",\"abbreviation\":\"DfE\",\"logo_formatted_name\":\"Department \\r\\nfor Education\",\"organisation_brand_colour_class_name\":\"department-for-education\",\"organisation_logo_type_class_name\":\"single-identity\",\"closed_at\":null,\"govuk_status\":\"live\",\"content_id\":\"ebd15ade-73b2-4eaf-b1c3-43034a42eb37\"},\"analytics_identifier\":\"D6\",\"parent_organisations\":[],\"child_organisations\":[{\"id\":\"https://www.gov.uk/api/organisations/education-funding-agency\",\"web_url\":\"https://www.gov.uk/government/organisations/education-funding-agency\"},{\"id\":\"https://www.gov.uk/api/organisations/standards-and-testing-agency\",\"web_url\":\"https://www.gov.uk/government/organisations/standards-and-testing-agency\"},{\"id\":\"https://www.gov.uk/api/organisations/ofqual\",\"web_url\":\"https://www.gov.uk/government/organisations/ofqual\"},{\"id\":\"https://www.gov.uk/api/organisations/ofsted\",\"web_url\":\"https://www.gov.uk/government/organisations/ofsted\"},{\"id\":\"https://www.gov.uk/api/organisations/office-of-the-children-s-commissioner\",\"web_url\":\"https://www.gov.uk/government/organisations/office-of-the-children-s-commissioner\"},{\"id\":\"https://www.gov.uk/api/organisations/school-teachers-review-body\",\"web_url\":\"https://www.gov.uk/government/organisations/school-teachers-review-body\"},{\"id\":\"https://www.gov.uk/api/organisations/social-mobility-and-child-poverty-commission\",\"web_url\":\"https://www.gov.uk/government/organisations/social-mobility-and-child-poverty-commission\"},{\"id\":\"https://www.gov.uk/api/organisations/national-college-for-teaching-and-leadership\",\"web_url\":\"https://www.gov.uk/government/organisations/national-college-for-teaching-and-leadership\"},{\"id\":\"https://www.gov.uk/api/organisations/national-college-for-school-leadership\",\"web_url\":\"https://www.gov.uk/government/organisations/national-college-for-school-leadership\"},{\"id\":\"https://www.gov.uk/api/organisations/office-of-the-schools-adjudicator\",\"web_url\":\"https://www.gov.uk/government/organisations/office-of-the-schools-adjudicator\"},{\"id\":\"https://www.gov.uk/api/organisations/schools-commissioners-group\",\"web_url\":\"https://www.gov.uk/government/organisations/schools-commissioners-group\"},{\"id\":\"https://www.gov.uk/api/organisations/government-equalities-office\",\"web_url\":\"https://www.gov.uk/government/organisations/government-equalities-office\"}],\"_response_info\":{\"status\":\"ok\",\"links\":[{\"href\":\"https://www.gov.uk/api/organisations/department-for-education\",\"rel\":\"self\"}]}}";

    @Rule
    public final DropwizardClientRule govukClientRule = new DropwizardClientRule(new TestGovukDepartmentForEducationResource());

    public Supplier<String> dfeHandler;
    @Path("/")
    public class TestGovukDepartmentForEducationResource {
        @GET
        @Path("/api/organisations/department-for-education")
        @Produces(MediaType.APPLICATION_JSON)
        public String dfe() {
            return dfeHandler.get();
        }
    }

    private GovukOrganisationClient organisationClient;

    @Before
    public void setUp() throws Exception {
        organisationClient = new GovukOrganisationClient(createTestJerseyClient(), govukClientRule::baseUri);
    }

    @Test
    public void getOrganisation_returnsDetailsFromApi() throws Exception {
        dfeHandler = () -> DEPARTMENT_FOR_EDUCATION_JSON;
        GovukOrganisation organisation = organisationClient.getOrganisation("department-for-education").get();
        assertThat(organisation.getDetails().getColourClassName(), equalTo("department-for-education"));
        assertThat(organisation.getDetails().getLogoClassName(), equalTo("single-identity"));
        assertThat(organisation.getDetails().getLogoFormattedName(), equalTo("Department \r\nfor Education"));
    }

    @Test
    public void getOrganisation_handlesTimeoutGracefully() throws Exception {
        dfeHandler = slowHandler(500);
        organisationClient = new GovukOrganisationClient(createTestJerseyClient(Duration.milliseconds(200)), govukClientRule::baseUri);

        Optional<GovukOrganisation> organisation = organisationClient.getOrganisation("department-for-education");

        assertThat(organisation, equalTo(empty()));
    }

    private Supplier<String> slowHandler(int millis) {
        return () -> {
            try {
                Thread.sleep(millis);
                return DEPARTMENT_FOR_EDUCATION_JSON;
            } catch (InterruptedException ignored) {

            }
            return null;
        };
    }

    @Test
    public void getOrganisation_handlesRemoteFailureGracefully() throws Exception {
        dfeHandler = () -> { throw new ServiceUnavailableException(); };
        Optional<GovukOrganisation> organisation = organisationClient.getOrganisation("department-for-education");
        assertThat(organisation, equalTo(empty()));
    }

    @Test
    public void getOrganisation_handlesNotFoundGracefully() throws Exception {
        dfeHandler = () -> { throw new NotFoundException(); };
        Optional<GovukOrganisation> organisation = organisationClient.getOrganisation("department-for-education");
        assertThat(organisation, equalTo(empty()));
    }
}
