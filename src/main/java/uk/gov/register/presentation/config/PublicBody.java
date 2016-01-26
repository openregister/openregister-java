package uk.gov.register.presentation.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.register.presentation.LinkValue;

@JsonIgnoreProperties({"text","crest","website","parent-bodies","official-colour","public-body-type"})
public class PublicBody {
    private final String name;
    private final String publicBodyId;

    @JsonCreator
    public PublicBody(@JsonProperty("name") String name, @JsonProperty("public-body") String publicBodyId) {
        this.name = name;
        this.publicBodyId = publicBodyId;
    }

    public String getName() {
        return name;
    }

    public String getPublicBodyId() {
        return publicBodyId;
    }

    @SuppressWarnings("unused, used from html templates")
    @JsonIgnore
    public String getRecordLink(){
        return new LinkValue("public-body", ".openregister.org", publicBodyId).link();
    }
}
