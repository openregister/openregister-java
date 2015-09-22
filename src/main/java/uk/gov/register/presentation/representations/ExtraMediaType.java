package uk.gov.register.presentation.representations;

import javax.ws.rs.core.MediaType;

public class ExtraMediaType {
    public static final String TEXT_HTML = "text/html; charset=utf-8";

    public static final String TEXT_CSV = "text/csv; charset=utf-8";
    public static final MediaType TEXT_CSV_TYPE = new MediaType("text", "csv", "utf-8");
    public static final String TEXT_TSV = "text/tab-separated-values; charset=utf-8";
    public static final MediaType TEXT_TSV_TYPE = new MediaType("text", "tab-separated-values", "utf-8");
    public static final String TEXT_TTL = "text/turtle; charset=utf-8";
    public static final MediaType TEXT_TTL_TYPE = new MediaType("text", "turtle", "utf-8");
    public static final String TEXT_YAML = "text/yaml; charset=utf-8";
    public static final MediaType TEXT_YAML_TYPE = new MediaType("text", "yaml", "utf-8");
}
