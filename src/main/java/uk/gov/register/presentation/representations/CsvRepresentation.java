package uk.gov.register.presentation.representations;

import com.fasterxml.jackson.dataformat.csv.CsvSchema;

public class CsvRepresentation<T> {
    public final CsvSchema csvSchema;
    public final Class contentType;
    public final T contents;

    public CsvRepresentation(CsvSchema csvSchema, T contents) {
        this.csvSchema = csvSchema;
        this.contents = contents;
        this.contentType = contents.getClass();
    }
}
