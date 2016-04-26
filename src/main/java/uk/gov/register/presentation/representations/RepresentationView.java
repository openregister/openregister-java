package uk.gov.register.presentation.representations;

import org.apache.jena.rdf.model.Model;

public interface RepresentationView<T> {
    CsvRepresentation<T> csvRepresentation();

    Model turtleRepresentation();
}
