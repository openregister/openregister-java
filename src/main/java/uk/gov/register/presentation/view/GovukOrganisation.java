package uk.gov.register.presentation.view;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GovukOrganisation {
    private static final Logger LOGGER = LoggerFactory.getLogger(GovukOrganisation.class);
    @JsonProperty
    private Details details;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Details {
        @JsonProperty("organisation_brand_colour_class_name")
        private Optional<String> colourClassName;

        @JsonProperty("organisation_logo_type_class_name")
        private String logoClassName;

        @JsonProperty("logo_formatted_name")
        private String logoFormattedName;

        @SuppressWarnings("unused, used by templates")
        public String getColourClassName() {
            return colourClassName.orElse("");
        }

        @SuppressWarnings("unused, used by templates")
        public String getLogoClassName() {
            return logoClassName;
        }

        @SuppressWarnings("unused, used by templates")
        public String getLogoFormattedName() {
            LOGGER.info("logo formatted name: \""+ logoFormattedName + "\"");
            return logoFormattedName.replaceAll("\r\n","<br/>");
        }
    }

    public Details getDetails() {
        return details;
    }
}
