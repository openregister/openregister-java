package uk.gov.register.resources;

import uk.gov.register.core.RegisterMetadata;
import uk.gov.register.core.RegisterName;
import uk.gov.register.core.RegisterReadOnly;
import uk.gov.register.service.ItemConverter;
import uk.gov.register.views.RecordView;
import uk.gov.register.views.ViewFactory;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;

@Path("/index/{index-name}")
public class DerivationRecordResource {
    private final HttpServletResponseAdapter httpServletResponseAdapter;
    private final RequestContext requestContext;
    private final RegisterReadOnly register;
    private final ViewFactory viewFactory;
    private final RegisterName registerPrimaryKey;
    private final ItemConverter itemConverter;

    @Inject
    public DerivationRecordResource(RegisterReadOnly register, ViewFactory viewFactory, RequestContext requestContext, ItemConverter itemConverter, RegisterMetadata registerMetadata) {
        this.register = register;
        this.viewFactory = viewFactory;
        this.requestContext = requestContext;
        this.httpServletResponseAdapter = new HttpServletResponseAdapter(requestContext.httpServletResponse);
        this.registerPrimaryKey = register.getRegisterName();
        this.itemConverter = itemConverter;
    }


    @GET
    @Path("/record/{record-key}")
    @Produces({MediaType.APPLICATION_JSON})
    public RecordView getRecordByKey(@PathParam("index-name") String indexName, @PathParam("record-key") String key) throws IOException {
        return register.getDerivationRecord(key, indexName).map(viewFactory::getRecordMediaView)
                .orElseThrow(NotFoundException::new);
    }


}
