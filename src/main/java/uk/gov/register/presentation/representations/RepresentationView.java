package uk.gov.register.presentation.representations;

public interface RepresentationView<T> {
    CsvRepresentation<T> csvRepresentation();
}
