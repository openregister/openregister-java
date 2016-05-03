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
import uk.gov.indexer.dao.Record;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AWSCloudSearch {

    private final String registerName;
    private final AmazonCloudSearchDomain cloudSearchData;
    private final AmazonCloudSearchDomain cloudSearchWatermark;

    public AWSCloudSearch(String registerName, String searchDataDomainEndPoint, String watermarkDomainEndPoint) {
        this.registerName = registerName;

        this.cloudSearchData = new AmazonCloudSearchDomainClient(new DefaultAWSCredentialsProviderChain());
        this.cloudSearchData.setRegion(Region.getRegion(Regions.EU_WEST_1));
        this.cloudSearchData.setEndpoint(searchDataDomainEndPoint);

        this.cloudSearchWatermark = new AmazonCloudSearchDomainClient(new DefaultAWSCredentialsProviderChain());
        this.cloudSearchWatermark.setRegion(Region.getRegion(Regions.EU_WEST_1));
        this.cloudSearchWatermark.setEndpoint(watermarkDomainEndPoint);
    }

    public int currentWaterMark() {
        SearchRequest searchRequest = new SearchRequest();
        //TODO: must search by id- remove temporary workaround i.e. kept extra field 'document_type' in document
        searchRequest.setQuery("watermark");
        searchRequest.setReturn("serial_number");
        SearchResult searchResult = cloudSearchWatermark.search(searchRequest);

        if (searchResult.getHits().getFound() == 0) {
            return 0;
        }

        return Integer.parseInt(searchResult.getHits().getHit().get(0).getFields().get("serial_number").get(0));
    }

    public void resetWatermark(int watermark) {
        byte[] bytes = Jackson.toJsonString(Collections.singletonList(watermarkDocument(watermark))).getBytes(StandardCharsets.UTF_8);

        UploadDocumentsResult uploadDocumentsResult = cloudSearchWatermark.uploadDocuments(createUploadDocumentsRequest(bytes));

        if (!"success".equalsIgnoreCase(uploadDocumentsResult.getStatus())) {
            throw new RuntimeException(registerName + " Watermark reset request failed: " + uploadDocumentsResult.toString());
        }

    }

    public void upload(List<Record> records) {
        byte[] bytes = Jackson.toJsonString(Lists.transform(records, this::entryDocument)).getBytes(StandardCharsets.UTF_8);

        UploadDocumentsResult uploadDocumentsResult = cloudSearchData.uploadDocuments(createUploadDocumentsRequest(bytes));

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

    private CloudSearchDocument entryDocument(Record record) {
        CloudSearchDocument cloudSearchDocument = new CloudSearchDocument();
        cloudSearchDocument.setID(record.item.getKey(registerName));
        cloudSearchDocument.setFields(record.item.getContent());
        return cloudSearchDocument;
    }

    private CloudSearchDocument watermarkDocument(int watermark) {
        CloudSearchDocument cloudSearchDocument = new CloudSearchDocument();
        cloudSearchDocument.setID("watermark");
        cloudSearchDocument.addIntField("serial_number", watermark);
        cloudSearchDocument.addField("document_type", "watermark");
        return cloudSearchDocument;
    }

    @SuppressWarnings("ALL")
    private static class CloudSearchDocument {
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

        void setFields(JsonNode fields) {
            Map<String, Object> fieldsMap = Jackson.fromJsonString(Jackson.toJsonString(fields), Map.class);
            this.fields = fieldsMap.keySet().stream().collect(Collectors.toMap(key -> key.replaceAll("-","_"), key -> fieldsMap.get(key)));
        }
    }
}

