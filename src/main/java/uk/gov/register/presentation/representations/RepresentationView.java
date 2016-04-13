package uk.gov.register.presentation.representations;

import com.fasterxml.jackson.dataformat.csv.CsvSchema;

public interface RepresentationView {

    CsvSchema csvSchema();
}
