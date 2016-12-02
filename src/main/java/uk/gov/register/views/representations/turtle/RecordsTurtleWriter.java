package uk.gov.register.views.representations.turtle;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import uk.gov.register.core.EverythingAboutARegister;
import uk.gov.register.core.RegisterResolver;
import uk.gov.register.views.RecordsView;
import uk.gov.register.views.representations.ExtraMediaType;

import javax.inject.Inject;
import javax.ws.rs.Produces;
import javax.ws.rs.ext.Provider;

@Provider
@Produces(ExtraMediaType.TEXT_TTL)
public class RecordsTurtleWriter extends TurtleRepresentationWriter<RecordsView> {

    @Inject
    public RecordsTurtleWriter(javax.inject.Provider<EverythingAboutARegister> aboutARegisterProvider, RegisterResolver registerResolver) {
        super(aboutARegisterProvider, registerResolver);
    }

    @Override
    protected Model rdfModelFor(RecordsView view) {
        Model model = ModelFactory.createDefaultModel();
        RecordTurtleWriter recordTurtleWriter = new RecordTurtleWriter(aboutARegisterProvider, registerResolver);
        view.getRecords().forEach(r -> model.add(recordTurtleWriter.rdfModelFor(r)));
        return model;
    }
}
