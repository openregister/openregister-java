package uk.gov.indexer;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.cloudsearchdomain.AmazonCloudSearchDomain;
import com.amazonaws.services.cloudsearchdomain.AmazonCloudSearchDomainClient;
import com.amazonaws.services.cloudsearchdomain.model.ContentType;
import com.amazonaws.services.cloudsearchdomain.model.UploadDocumentsRequest;
import com.amazonaws.services.cloudsearchdomain.model.UploadDocumentsResult;
import com.amazonaws.util.json.Jackson;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;
import uk.gov.indexer.dao.Entry;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class AWSCloudSearch {

    private final AmazonCloudSearchDomain cloudSearchDomain;
    private final String registerName;

    public AWSCloudSearch(String registerName, String searchDomainEndPoint) {
        this.registerName = registerName;
        this.cloudSearchDomain = new AmazonCloudSearchDomainClient(new DefaultAWSCredentialsProviderChain());
        this.cloudSearchDomain.setRegion(Region.getRegion(Regions.EU_WEST_1));
        this.cloudSearchDomain.setEndpoint(searchDomainEndPoint);
    }

    public void upload(List<Entry> entries) {
        byte[] bytes = Jackson.toJsonString(Lists.transform(entries, this::document)).getBytes(StandardCharsets.UTF_8);
        UploadDocumentsRequest uploadDocumentsRequest = new UploadDocumentsRequest();
        uploadDocumentsRequest.setContentType(ContentType.Applicationjson);
        uploadDocumentsRequest.setContentLength((long) bytes.length);
        uploadDocumentsRequest.setDocuments(new ByteArrayInputStream(bytes));
        UploadDocumentsResult uploadDocumentsResult = cloudSearchDomain.uploadDocuments(uploadDocumentsRequest);

        if (!"success".equalsIgnoreCase(uploadDocumentsResult.getStatus())) {
            throw new RuntimeException("Address upload request failed: " + uploadDocumentsResult.toString());
        }
    }

    private ObjectNode document(Entry e) {
        JsonNode jsonNode = Jackson.jsonNodeOf(new String(e.contents, StandardCharsets.UTF_8));
        ObjectNode objectNode = JsonNodeFactory.instance.objectNode();
        objectNode.put("type", "add");
        objectNode.put("id", jsonNode.get("entry").get(registerName).textValue());
        //TODO: this should be document with hash
        objectNode.set("fields", jsonNode.get("entry"));
        return objectNode;
    }
}
