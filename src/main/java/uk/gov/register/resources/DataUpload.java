package uk.gov.register.resources;

import com.codahale.metrics.annotation.Timed;
import uk.gov.register.exceptions.RSFParseException;
import uk.gov.register.serialization.RSFFormatter;
import uk.gov.register.serialization.RegisterResult;
import uk.gov.register.serialization.RegisterSerialisationFormat;
import uk.gov.register.service.RegisterSerialisationFormatService;
import uk.gov.register.views.representations.ExtraMediaType;

import javax.annotation.security.PermitAll;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;

@Path("/")
public class DataUpload {
    private final RegisterSerialisationFormatService rsfService;
    private final RSFFormatter rsfFormatter;

    @Inject
    public DataUpload(RegisterSerialisationFormatService rsfService) {
        this.rsfService = rsfService;
        this.rsfFormatter = new RSFFormatter();
    }

    @Context
    HttpServletRequest httpServletRequest;

    @POST
    @PermitAll
    @Consumes(ExtraMediaType.APPLICATION_RSF)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/load-rsf")
    @Timed
    public Response loadRsf(InputStream inputStream) throws RSFParseException {
        RegisterSerialisationFormat rsf = rsfService.readFrom(inputStream, rsfFormatter);
        rsfService.process(rsf);
        return Response.status(Response.Status.OK).entity(RegisterResult.createSuccessResult()).build();
    }
}
