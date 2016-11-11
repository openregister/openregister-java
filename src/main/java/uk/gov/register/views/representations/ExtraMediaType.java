package uk.gov.register.views.representations;

import javax.ws.rs.core.MediaType;

public class ExtraMediaType {
    public static final String TEXT_HTML = "text/html; charset=UTF-8";

    public static final String TEXT_CSV = "text/csv; charset=UTF-8";
    public static final MediaType TEXT_CSV_TYPE = new MediaType("text", "csv", "UTF-8");
    public static final String TEXT_TSV = "text/tab-separated-values; charset=UTF-8";
    public static final MediaType TEXT_TSV_TYPE = new MediaType("text", "tab-separated-values", "UTF-8");
    public static final String TEXT_TTL = "text/turtle; charset=UTF-8";
    public static final MediaType TEXT_TTL_TYPE = new MediaType("text", "turtle", "UTF-8");
    public static final String TEXT_YAML = "text/yaml; charset=UTF-8";
    public static final MediaType TEXT_YAML_TYPE = new MediaType("text", "yaml", "UTF-8");
    public static final String APPLICATION_RSF = "application/uk-gov-rsf";
    public static final MediaType APPLICATION_RSF_TYPE = new MediaType("application", "uk-gov-rsf", "UTF-8");
}
