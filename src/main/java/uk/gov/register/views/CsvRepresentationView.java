package uk.gov.register.views;

import uk.gov.register.views.representations.CsvRepresentation;

public interface CsvRepresentationView<T> {
    CsvRepresentation<T> csvRepresentation();
}
