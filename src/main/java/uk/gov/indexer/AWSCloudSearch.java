package uk.gov.indexer;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.cloudsearchdomain.AmazonCloudSearchDomain;
import com.amazonaws.services.cloudsearchdomain.AmazonCloudSearchDomainClient;
import com.amazonaws.services.cloudsearchdomain.model.*;
import com.amazonaws.util.json.Jackson;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import uk.gov.indexer.dao.OrderedEntryIndex;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AWSCloudSearch {

    private final String registerName;
    private final AmazonCloudSearchDomain cloudSearchDomain;
    private final AmazonCloudSearchDomain cloudSearchWatermarkDomain;

    public AWSCloudSearch(String registerName, String searchDomainEndPoint, String searchDomainWatermarkEndPoint) {
        this.registerName = registerName;

        this.cloudSearchDomain = new AmazonCloudSearchDomainClient(new DefaultAWSCredentialsProviderChain());
        this.cloudSearchDomain.setRegion(Region.getRegion(Regions.EU_WEST_1));
        this.cloudSearchDomain.setEndpoint(searchDomainEndPoint);

        this.cloudSearchWatermarkDomain = new AmazonCloudSearchDomainClient(new DefaultAWSCredentialsProviderChain());
        this.cloudSearchWatermarkDomain.setRegion(Region.getRegion(Regions.EU_WEST_1));
        this.cloudSearchWatermarkDomain.setEndpoint(searchDomainWatermarkEndPoint);
    }

    int currentWaterMark() {
        SearchRequest searchRequest = new SearchRequest();
        //TODO: must search by id
        searchRequest.setQuery("watermark");
        searchRequest.setReturn("serial_number");
        SearchResult searchResult = cloudSearchWatermarkDomain.search(searchRequest);

        if (searchResult.getHits().getFound() == 0) {
            return 0;
        }

        return Integer.parseInt(searchResult.getHits().getHit().get(0).getFields().get("serial_number").get(0));
    }

    public void resetWatermark(int watermark) {
        byte[] bytes = Jackson.toJsonString(Collections.singletonList(watermarkDocument(watermark))).getBytes(StandardCharsets.UTF_8);

        UploadDocumentsResult uploadDocumentsResult = cloudSearchWatermarkDomain.uploadDocuments(createUploadDocumentsRequest(bytes));

        if (!"success".equalsIgnoreCase(uploadDocumentsResult.getStatus())) {
            throw new RuntimeException(registerName + " Watermark upload request failed: " + uploadDocumentsResult.toString());
        }

    }

    public void upload(List<OrderedEntryIndex> entries) {
        byte[] bytes = Jackson.toJsonString(Lists.transform(entries, this::entryDocument)).getBytes(StandardCharsets.UTF_8);

        UploadDocumentsResult uploadDocumentsResult = cloudSearchDomain.uploadDocuments(createUploadDocumentsRequest(bytes));

        if (!"success".equalsIgnoreCase(uploadDocumentsResult.getStatus())) {
            throw new RuntimeException(registerName + " document upload request failed: " + uploadDocumentsResult.toString());
        }
    }

    private UploadDocumentsRequest createUploadDocumentsRequest(byte[] bytes) {
        UploadDocumentsRequest uploadDocumentsRequest = new UploadDocumentsRequest();
        uploadDocumentsRequest.setContentType(ContentType.Applicationjson);
        uploadDocumentsRequest.setContentLength((long) bytes.length);
        uploadDocumentsRequest.setDocuments(new ByteArrayInputStream(bytes));
        return uploadDocumentsRequest;
    }

    private CloudSearchDocument entryDocument(OrderedEntryIndex e) {
        JsonNode jsonNode = Jackson.jsonNodeOf(e.getEntry());
        CloudSearchDocument cloudSearchDocument = new CloudSearchDocument();
        cloudSearchDocument.setID(jsonNode.get("entry").get(registerName).textValue());
        cloudSearchDocument.setFields(jsonNode.get("entry"));
        return cloudSearchDocument;
    }

    private CloudSearchDocument watermarkDocument(int watermark) {
        CloudSearchDocument cloudSearchDocument = new CloudSearchDocument();
        cloudSearchDocument.setID("watermark");
        cloudSearchDocument.addIntField("serial_number", watermark);
        cloudSearchDocument.addField("document_type", "watermark");
        return cloudSearchDocument;
    }
}

class CloudSearchDocument {
    @JsonProperty
    private final String type = "add";
    @JsonProperty
    private String id;
    @JsonProperty
    private Map<String, Object> fields = new HashMap<>();

    void setID(String id) {
        this.id = id;
    }

    void addIntField(String fieldName, int value) {
        fields.put(fieldName, value);
    }

    void addField(String fieldName, String value) {
        fields.put(fieldName, value);
    }

    @SuppressWarnings("unchecked")
    public void setFields(JsonNode fields) {
        this.fields = Jackson.fromJsonString(Jackson.toJsonString(fields), Map.class);
    }
}
