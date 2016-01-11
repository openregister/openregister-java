package uk.gov.indexer;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableMap;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.client.JerseyClientBuilder;
import uk.gov.indexer.dao.OrderedEntryIndex;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

public class ElasticSearch {
    private final String register;
    private final JerseyClient client;
    private final String searchDomainEndPoint;

    public ElasticSearch(String register, String searchDomainEndPoint) {
        this.searchDomainEndPoint = searchDomainEndPoint;
        this.client = JerseyClientBuilder.createClient(new ClientConfig());
        this.register = register;
    }

    public void upload(List<OrderedEntryIndex> entries) {

        entries.forEach(entry -> {

            Map<String, ?> uploadRequest = uploadRequest(entry);

            String entryPrimaryKey = (String) ((Map) uploadRequest.get("entry")).get(register);

            String requestUri = String.format("%s/%s/records/%s", searchDomainEndPoint, register, entryPrimaryKey);

            Response response = makeRequest(requestUri, uploadRequest);


            if (Response.Status.Family.SUCCESSFUL != Response.Status.fromStatusCode(response.getStatus()).getFamily()) {
                throw new RuntimeException(
                        String.format(
                                "Error while loading entry to elasticsearch for register: '%s'. error entry serial_number is: '%s' and entry is: '%s'",
                                register,
                                entry.getSerial_number(),
                                entry.getEntry()
                        )
                );
            }
        });
    }

    public int currentWaterMark() {
        String requestUri = String.format("%s/%s/records/_search", searchDomainEndPoint, register);

        Response response = makeRequest(requestUri, highestSerialNumberSearchRequest());

        //note: 404 will be returned when requested index is not exists.
        //we are returning 0 which confirms that there is no entry and the index will be created when first entry is uploaded.
        if (response.getStatus() == 404) {
            return 0;
        }

        if (response.getStatus() == 200) {
            JsonNode responseJson = JsonUtils.fromJsonString(response.readEntity(String.class), JsonNode.class);

            if (responseJson.get("hits").get("total").intValue() == 0) {
                return 0;
            }

            return responseJson.get("hits").get("hits").get(0).get("_source").get("serial_number").intValue();
        }

        throw new RuntimeException("Unexpected response from elasticsearch request. statusCode: " + response.getStatus() + ", response body: " + response.readEntity(String.class));
    }

    private Response makeRequest(String requestUri, Map<String, ?> entity) {
        return client
                .target(requestUri)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(JsonUtils.toJsonString(entity), MediaType.APPLICATION_JSON_TYPE));
    }

    private Map<String, ?> highestSerialNumberSearchRequest() {
        return ImmutableMap.of(
                "filter", ImmutableMap.of(
                        "match_all", ImmutableMap.of()
                ),
                "sort", ImmutableMap.of(
                        "serial_number", ImmutableMap.of(
                                "order", "desc"
                        )
                ),
                "size", 1
        );
    }

    @SuppressWarnings("unchecked")
    private Map<String, ?> uploadRequest(OrderedEntryIndex orderedEntryIndex) {
        Map requestMap = JsonUtils.fromJsonString(orderedEntryIndex.getEntry(), Map.class);
        requestMap.put("serial_number", orderedEntryIndex.getSerial_number());
        return requestMap;
    }
}

