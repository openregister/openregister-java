package uk.gov.register.functional;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.codec.digest.DigestUtils;
import org.json.JSONException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import uk.gov.register.functional.app.RegisterRule;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static uk.gov.register.functional.app.TestRegister.address;
import static uk.gov.register.views.representations.ExtraMediaType.TEXT_HTML;

public class RecordListResourceFunctionalTest {
    @ClassRule
    public static RegisterRule register = new RegisterRule();
    private final WebTarget addressTarget = register.target(address);

    @Before
    public void publishTestMessages() {
        register.wipe();
        register.mintLines(address, "{\"street\":\"ellis\",\"address\":\"12345\"}", "{\"street\":\"presley\",\"address\":\"6789\"}", "{\"street\":\"ellis\",\"address\":\"145678\"}", "{\"street\":\"updatedEllisName\",\"address\":\"145678\"}", "{\"street\":\"ellis\",\"address\":\"6789\"}");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void newRecords_shouldReturnAllCurrentVersionsOnly() throws Exception {
        Response response = register.getRequest(address, "/records.json");

        Map<String, Map<String, Object>> responseMap = response.readEntity(Map.class);

        assertThat(responseMap.size(), equalTo(3));

        Map<String,Object> map1 = responseMap.get("6789");
        assertThat(map1.get("entry-number"), is("5"));
        List<String> hashes = (List<String>) map1.get("item-hashes");
        assertThat(hashes.get(0), is("sha-256:" + DigestUtils.sha256Hex("{\"address\":\"6789\",\"street\":\"ellis\"}")));
        List items1 = (List) map1.get("items");
        assertThat(items1.size(), is(1));
        Map<String, String> itemMap1 = (Map<String, String>) items1.get(0);
        assertThat(itemMap1.get("street"), is("ellis"));
        assertThat(itemMap1.get("address"), is("6789"));

        Map<String,Object> map2 = responseMap.get("145678");
        assertThat(map2.get("entry-number"), is("4"));
        List<String> hashes2 = (List<String>) map2.get("item-hashes");
        assertThat(hashes2.get(0), is("sha-256:" + DigestUtils.sha256Hex("{\"address\":\"145678\",\"street\":\"updatedEllisName\"}")));
        List items2 = (List) map2.get("items");
        assertThat(items2.size(), is(1));
        Map<String, String> itemMap2 = (Map<String, String>) items2.get(0);
        assertThat(itemMap2.get("street"), is("updatedEllisName"));
        assertThat(itemMap2.get("address"), is("145678"));

        Map<String,Object> map3 = responseMap.get("12345");
        assertThat(map3.get("entry-number"), is("1"));
        List<String> hashes3 = (List<String>) map3.get("item-hashes");
        assertThat(hashes3.get(0), is("sha-256:" + DigestUtils.sha256Hex("{\"address\":\"12345\",\"street\":\"ellis\"}")));
        List items3 = (List) map3.get("items");
        assertThat(items3.size(), is(1));
        Map<String, String> itemMap3 = (Map<String, String>) items3.get(0);
        assertThat(itemMap3.get("street"), is("ellis"));
        assertThat(itemMap3.get("address"), is("12345"));

    }

    @Test
    public void newRecords_setsAppropriateFilenameForDownload() {
        Response response = register.getRequest(address, "/records.json");
        assertThat(response.getStatus(), equalTo(200));
        assertThat(response.getHeaderString(HttpHeaders.CONTENT_DISPOSITION), containsString("filename=\"address-records.json\""));
    }

    @Test
    public void newRecords_hasLinkHeaderForNextAndPreviousPage() {
        Response response = addressTarget.path("/records.json").queryParam("page-index",1).queryParam("page-size",1)
                .request().get();
        assertThat(response.getHeaderString("Link"), equalTo("<?page-index=2&page-size=1>; rel=\"next\""));

        response = addressTarget.path("/records.json").queryParam("page-index",2).queryParam("page-size",1)
                .request().get();
        assertThat(response.getHeaderString("Link"), equalTo("<?page-index=3&page-size=1>; rel=\"next\",<?page-index=1&page-size=1>; rel=\"previous\""));

        response = addressTarget.path("/records.json").queryParam("page-index",3).queryParam("page-size",1)
                .request().get();
        assertThat(response.getHeaderString("Link"), equalTo("<?page-index=2&page-size=1>; rel=\"previous\""));
    }

    @Test
    public void newRecordsPageHasXhtmlLangAttributes() {
        Response response = register.getRequest(address, "/records", TEXT_HTML);

        Document doc = Jsoup.parse(response.readEntity(String.class));
        Elements htmlElement = doc.select("html");
        assertThat(htmlElement.size(), equalTo(1));
        assertThat(htmlElement.first().attr("lang"), equalTo("en"));
        assertThat(htmlElement.first().attr("xml:lang"), equalTo("en"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void fetchAllRecordsForAKeyValueCombination() throws JSONException {
        Response response = register.getRequest(address, "/records/street/ellis.json");
        Map<String, Map<String, Object>> responseMap = response.readEntity(Map.class);

        assertThat(responseMap.size(), equalTo(2));

        Map<String,Object> map1 = responseMap.get("6789");

        assertThat(map1.get("entry-number"), is("5"));

        List<String> hashes = (List<String>) map1.get("item-hashes");
        assertThat(hashes.get(0), is("sha-256:" + DigestUtils.sha256Hex("{\"address\":\"6789\",\"street\":\"ellis\"}")));
        List items = (List) map1.get("items");
        assertThat(items.size(), is(1));
        Map<String, String> itemMap = (Map<String, String>) items.get(0);
        assertThat(itemMap.get("street"), is("ellis"));
        assertThat(itemMap.get("address"), is("6789"));

        Map<String,Object> map3 = responseMap.get("12345");
        assertThat(map3.get("entry-number"), is("1"));
        List<String> hashes3 = (List<String>) map3.get("item-hashes");
        assertThat(hashes3.get(0), is("sha-256:" + DigestUtils.sha256Hex("{\"address\":\"12345\",\"street\":\"ellis\"}")));
        List items3 = (List) map3.get("items");
        assertThat(items3.size(), is(1));
        Map<String, String> itemMap3 = (Map<String, String>) items3.get(0);
        assertThat(itemMap3.get("street"), is("ellis"));
        assertThat(itemMap3.get("address"), is("12345"));

    }

    //Note: tests below will be removed once the old resources are deleted
    @Test
    public void oldFacetedResourceRedirectsToNewResource(){
        addressTarget.property("jersey.config.client.followRedirects",false);
        Response response = addressTarget.path("/street/ellis.json").request().get();
        assertThat(response.getStatus(), equalTo(301));
        String expectedRedirect = "/records/street/ellis";
        URI location = URI.create(response.getHeaderString("Location"));
        assertThat(location.getPath(), equalTo(expectedRedirect));
    }
}

