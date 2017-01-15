package uk.gov.register.resources;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.register.core.Entry;
import uk.gov.register.core.Item;
import uk.gov.register.core.Register;
import uk.gov.register.core.RegisterContext;
import uk.gov.register.exceptions.SerializedRegisterParseException;
import uk.gov.register.serialization.RSFFormatter;
import uk.gov.register.serialization.RSFResult;
import uk.gov.register.serialization.RegisterSerialisationFormat;
import uk.gov.register.service.RegisterSerialisationFormatService;
import uk.gov.register.util.ObjectReconstructor;
import uk.gov.register.views.representations.ExtraMediaType;

import javax.annotation.security.PermitAll;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import java.io.InputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


@Path("/")
public class DataUpload {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ObjectReconstructor objectReconstructor;
    private final RegisterSerialisationFormatService rsfService;
    private final RegisterContext registerContext;
    private final RSFFormatter rsfFormatter;


    @Inject
    public DataUpload(ObjectReconstructor objectReconstructor, RegisterSerialisationFormatService rsfService, RegisterContext registerContext) {
        this.objectReconstructor = objectReconstructor;
        this.rsfService = rsfService;
        this.registerContext = registerContext;
        this.rsfFormatter = new RSFFormatter();
    }

    @Context
    HttpServletRequest httpServletRequest;

    @POST
    @PermitAll
    @Path("/load")
    public void load(String payload) {
        try {
            Iterable<JsonNode> objects = objectReconstructor.reconstructWithCanonicalization(payload.split("\n"));
            mintItems(objects);
        } catch (Throwable t) {
            logger.error(Throwables.getStackTraceAsString(t));
            throw t;
        }
    }

    @POST
    @PermitAll
    @Consumes(ExtraMediaType.APPLICATION_RSF)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/load-rsf")
    public Response loadRsf(InputStream inputStream) {
        RSFResult loadResult;
        try {
            RegisterSerialisationFormat rsf = rsfService.readFrom(inputStream, rsfFormatter);
            loadResult = rsfService.processRegisterSerialisationFormat(rsf);
        } catch (SerializedRegisterParseException e) {
            // cathing only RSF parsing exceptions and handling those
            loadResult = RSFResult.createFailResult("RSF parsing error", e);
        }


        if (loadResult.isSuccessful()) {
            return Response.status(Response.Status.OK).build();
        } else {
            // this is most of the time not 500
            return Response.status(500).entity(loadResult).build();
        }


    }

    private void mintItems(Iterable<JsonNode> objects) {
        registerContext.transactionalRegisterOperation(register -> {
            AtomicInteger currentEntryNumber = new AtomicInteger(register.getTotalEntries());
            Iterables.transform(objects, Item::new).forEach(item -> mintItem(register, currentEntryNumber, item));
        });
    }

    private void mintItem(Register register, AtomicInteger currentEntryNumber, Item item) {
        register.putItem(item);
        register.appendEntry(new Entry(currentEntryNumber.incrementAndGet(), item.getSha256hex(), Instant.now(), item.getValue(this.registerContext.getRegisterName().value())));
    }
}

