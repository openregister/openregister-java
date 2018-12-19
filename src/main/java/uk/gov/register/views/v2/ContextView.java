package uk.gov.register.views.v2;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import uk.gov.register.core.EntryType;
import uk.gov.register.core.Field;
import uk.gov.register.core.RegisterContext;
import uk.gov.register.core.RegisterMetadata;
import uk.gov.register.core.RegisterReadOnly;
import uk.gov.register.proofs.ProofGenerator;
import uk.gov.register.util.ISODateFormatter;
import uk.gov.register.views.RegisterProof;

import java.time.Instant;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@JsonPropertyOrder({"id", "copyright", "licence", "custodian", "title", "description", "hashing-algorithm", "rootHash", "schema", "statistics", "status"})
@JsonInclude(JsonInclude.Include.NON_ABSENT)
public class ContextView {
    private final Collection<Field> fields;
    private final int totalRecords;
    private final int totalEntries;
    private final int totalBlobs;
    private final Optional<String> custodianName;
    private final String id;
    private final String rootHash;
    private final Optional<String> licence;
    private final Optional<String> copyright;
    private final Optional<String> description;
    private final Optional<Instant> startDate;
    private final Optional<String> title;

    public ContextView(
            RegisterContext registerContext
    ) {
        RegisterReadOnly register = registerContext.buildOnDemandRegister();
        RegisterMetadata registerMetadata = register.getRegisterMetadata();

        this.fields = register.getFieldsByName().values();
        this.totalRecords = register.getTotalRecords(EntryType.user);
        this.totalEntries = register.getTotalEntries(EntryType.user);
        this.totalBlobs = register.getTotalItems();
        this.custodianName = register.getCustodianName();
        this.id = register.getRegisterId().value();
        this.title = register.getRegisterName();

        this.copyright = Optional.ofNullable(registerMetadata.getCopyright());
        this.licence = Optional.empty();
        this.description = Optional.ofNullable(registerMetadata.getText());

        this.rootHash = registerContext.withVerifiableLog(verifiableLog -> {
            RegisterProof proof = new ProofGenerator(verifiableLog).getRegisterProof(this.totalEntries);

            return proof.getRootHash().multihash();
        });

        this.startDate = register.getEntry(1).map(entry -> entry.getTimestamp());
    }

    @JsonProperty
    public Optional<String> getCopyright() {
        return this.copyright;
    }

    @JsonProperty
    public String getId() {
        return this.id;
    }

    @JsonProperty
    public Optional<String> getTitle() {
        return this.title;
    }

    @JsonProperty
    public Optional<String> getCustodian() {
        return this.custodianName;
    }

    @JsonProperty
    public Optional<String> getDescription() {
        return this.description;
    }

    @JsonProperty("hashing-algorithm")
    public Map<String, Object> getHashingAlgorithm() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", "sha2-256");
        result.put("function-type", 18);
        result.put("digest-length", 32);
        return result;
    }

    @JsonProperty
    public Optional<String> getLicence() {
        return this.licence;
    }

    @JsonProperty("root-hash")
    public String getRootHash() {
        return rootHash;
    }

    @JsonProperty
    public Map<String, Integer> getStatistics() {
        Map<String, Integer> result = new LinkedHashMap<>();
        result.put("total-records", this.totalRecords);
        result.put("total-entries", this.totalEntries);
        result.put("total-blobs", this.totalBlobs);
        return result;
    }

    @JsonProperty
    public Map<String, Object> getStatus() {
        Map<String, Object> result = new LinkedHashMap<>();
        if(startDate.isPresent()) {
            result.put("start-date", ISODateFormatter.format(startDate.get()));
        }
        return result;
    }

    @JsonProperty
    public List<Map<String, Object>> getSchema() {
        return fields.stream().map(field -> {
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("id", field.fieldName);
            result.put("datatype", field.getDatatype().getName());
            result.put("cardinality", field.getCardinality().getId());
            return result;
        }).collect(Collectors.toList());
    }
}
