package uk.gov.register.resources;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.register.core.*;
import uk.gov.register.exceptions.ItemValidationException;
import uk.gov.register.exceptions.SerializedRegisterParseException;
import uk.gov.register.serialization.RSFFormatter;
import uk.gov.register.serialization.RegisterResult;
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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

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
    @Timed
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
    @Timed
    public Response loadRsf(InputStream inputStream) {
        RegisterResult loadResult;
        try {
            RegisterSerialisationFormat rsf = rsfService.readFrom(inputStream, rsfFormatter);
            loadResult = rsfService.process(rsf);
            // catching only RSF parsing exceptions and handling those
        } catch (SerializedRegisterParseException e) {
            loadResult = RegisterResult.createFailResult("RSF parsing error", e);
        }

        if (loadResult.isSuccessful()) {
            return Response.status(Response.Status.OK).build();
        } else {
            return Response.status(400).entity(loadResult).build();
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
        String key = item.getValue(this.registerContext.getRegisterName().value())
                .orElseThrow(() -> new ItemValidationException("Item did not contain key field", item.getContent()));

        register.appendEntry(new Entry(currentEntryNumber.incrementAndGet(), item.getSha256hex(), Instant.now(), key, EntryType.user));
    }
}
