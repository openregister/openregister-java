package uk.gov.indexer;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Rule;
import org.junit.Test;
import uk.gov.indexer.dao.OrderedEntryIndex;

import javax.ws.rs.core.MediaType;

import java.util.Arrays;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class ElasticSearchTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(9111);

    private ElasticSearch elasticSearch = new ElasticSearch("register", "http://localhost:9111");

    @Test
    public void currentWaterMark_returns0WhenNoIndexIsAvailableForRegister() {
        stubFor(
                post(urlPathEqualTo("/register/records/_search"))
                        .withRequestBody(equalToJson("{\"filter\":{\"match_all\":{}}, \"sort\":{\"serial_number\":{\"order\":\"desc\"}}, \"size\": 1}"))
                        .willReturn(
                                ResponseDefinitionBuilder.responseDefinition()
                                        .withStatus(404)
                        )
        );

        assertThat(elasticSearch.currentWaterMark(), equalTo(0));
    }

    @Test
    public void currentWaterMark_returns0WhenIndexIsAvailableButNoEntryExists() {
        stubFor(
                post(urlPathEqualTo("/register/records/_search"))
                        .withRequestBody(equalToJson("{\"filter\":{\"match_all\":{}}, \"sort\":{\"serial_number\":{\"order\":\"desc\"}}, \"size\": 1}"))
                        .willReturn(
                                ResponseDefinitionBuilder.responseDefinition()
                                        .withBody("{'took':1,'timed_out':false,'_shards':{'total':5,'successful':5,'failed':0},'hits':{'total':0,'max_score':null,'hits':[]}}".replaceAll("'", "\""))
                                        .withHeader("Content-Type", MediaType.APPLICATION_JSON)
                                        .withStatus(200)
                        )
        );

        assertThat(elasticSearch.currentWaterMark(), equalTo(0));
    }

    @Test
    public void currentWaterMark_returnsTheHighestSerialNumberOfAllEntries() {

        stubFor(
                post(urlPathEqualTo("/register/records/_search"))
                        .withRequestBody(equalToJson("{\"filter\":{\"match_all\":{}}, \"sort\":{\"serial_number\":{\"order\":\"desc\"}}, \"size\": 1}"))
                        .willReturn(
                                ResponseDefinitionBuilder.responseDefinition()
                                        .withBody("{'took':1,'timed_out':false,'_shards':{'total':5,'successful':5,'failed':0},'hits':{'total':1,'max_score':null,'hits':[{'_index':'register','_type':'records','_id':'foo','_score':1.0,'_source':{'serial_number': 5, 'hash':'hashvalue', 'entry': {'register':'foo', 'text':'textvalue'}}}]}}".replaceAll("'", "\""))
                                        .withHeader("Content-Type", MediaType.APPLICATION_JSON)
                                        .withStatus(200)
                        )
        );


        assertThat(elasticSearch.currentWaterMark(), equalTo(5));
    }

    @Test(expected = RuntimeException.class)
    public void currentWaterMark_throwsRuntimeExceptionWhenESReturnsNon200OrNon404Response() {
        stubFor(
                post(urlPathEqualTo("/register/records/_search"))
                        .withRequestBody(equalToJson("{\"filter\":{\"match_all\":{}}, \"sort\":{\"serial_number\":{\"order\":\"desc\"}}, \"size\": 1}"))
                        .willReturn(
                                ResponseDefinitionBuilder.responseDefinition()
                                        .withStatus(500)
                        )
        );

        elasticSearch.currentWaterMark();
    }

    @Test
    public void upload_postsAllEntriesToElasticSearchDomain() {
        stubFor(
                post(urlPathEqualTo("/register/records/foo1"))
                        .withRequestBody(equalToJson("{\"serial_number\":1, \"hash\":\"hashValue1\", \"entry\":{\"register\":\"foo1\", \"text\":\"textValue1\"}}"))
                        .willReturn(ResponseDefinitionBuilder.responseDefinition().withStatus(201))
        );

        stubFor(
                post(urlPathEqualTo("/register/records/foo2"))
                        .withRequestBody(equalToJson("{\"serial_number\":2,\"hash\":\"hashValue2\", \"entry\":{\"register\":\"foo2\", \"text\":\"textValue2\"}}"))
                        .willReturn(ResponseDefinitionBuilder.responseDefinition().withStatus(201))
        );

        stubFor(
                post(urlPathEqualTo("/register/records/foo1"))
                        .withRequestBody(equalToJson("{\"serial_number\":3,\"hash\":\"hashValue3\", \"entry\":{\"register\":\"foo1\", \"text\":\"updatetextValue\"}}"))
                        .willReturn(ResponseDefinitionBuilder.responseDefinition().withStatus(200))
        );

        List<OrderedEntryIndex> entries = Arrays.asList(
                new OrderedEntryIndex(1, "{\"hash\":\"hashValue1\", \"entry\":{\"register\":\"foo1\", \"text\":\"textValue1\"}}", "leaf_input_value"),
                new OrderedEntryIndex(2, "{\"hash\":\"hashValue2\", \"entry\":{\"register\":\"foo2\", \"text\":\"textValue2\"}}", "leaf_input_value"),
                new OrderedEntryIndex(3, "{\"hash\":\"hashValue3\", \"entry\":{\"register\":\"foo1\", \"text\":\"updatetextValue\"}}", "leaf_input_value")
        );

        elasticSearch.upload(entries);
    }


    @Test
    public void upload_throwsRuntimeExceptionWhenAnyEntryUploadIsNotSuccessful() {
        stubFor(
                post(urlPathEqualTo("/register/records/foo1"))
                        .withRequestBody(equalToJson("{\"serial_number\":1, \"hash\":\"hashValue1\", \"entry\":{\"register\":\"foo1\", \"text\":\"textValue1\"}}"))
                        .willReturn(ResponseDefinitionBuilder.responseDefinition().withStatus(201))
        );

        stubFor(
                post(urlPathEqualTo("/register/records/foo2"))
                        .withRequestBody(equalToJson("{\"serial_number\":2,\"hash\":\"hashValue2\", \"entry\":{\"register\":\"foo2\", \"text\":\"textValue2\"}}"))
                        .willReturn(ResponseDefinitionBuilder.responseDefinition().withStatus(400))
        );

        List<OrderedEntryIndex> entries = Arrays.asList(
                new OrderedEntryIndex(1, "{\"hash\":\"hashValue1\", \"entry\":{\"register\":\"foo1\", \"text\":\"textValue1\"}}", "leaf_input_value"),
                new OrderedEntryIndex(2, "{\"hash\":\"hashValue2\", \"entry\":{\"register\":\"foo2\", \"text\":\"textValue2\"}}", "leaf_input_value")
        );

        try {
            elasticSearch.upload(entries);
            fail("must throw exception");
        } catch (RuntimeException e) {
            assertThat(e.getMessage(), containsString("serial_number is: '2'"));
        }
    }
}
