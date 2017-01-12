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
import uk.gov.register.serialization.RSFFormat;
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
import javax.ws.rs.core.Context;
import java.io.InputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import javax.ws.rs.core.Response;


@Path("/")
public class DataUpload {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ObjectReconstructor objectReconstructor;
    private final RegisterSerialisationFormatService rsfService;
    private final RegisterContext registerContext;
    private final RSFFormat rsfFormat;


    @Inject
    public DataUpload(ObjectReconstructor objectReconstructor, RegisterSerialisationFormatService rsfService, RegisterContext registerContext) {
        this.objectReconstructor = objectReconstructor;
        this.rsfService = rsfService;
        this.registerContext = registerContext;
        this.rsfFormat = new RSFFormat();
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
    @Path("/load-rsf")
    public Response loadRsf(InputStream inputStream) {
        // parsing exceptions try catch ok
        List<Exception> exceptions = new ArrayList();
        try {
            RegisterSerialisationFormat rsf = rsfService.readFrom(inputStream, rsfFormat);
            List<Exception> rsfServiceExp = rsfService.processRegisterSerialisationFormat(rsf);
            exceptions.addAll(rsfServiceExp);
        } catch (Exception e) {
            exceptions.add(e);
        }
        // better handling

        exceptions.forEach(e -> System.out.println(e.getMessage()));
        String exceptionMessages = exceptions.stream().map(e -> e.getMessage()).collect(Collectors.joining(", "));

        return exceptions.isEmpty() ? Response.status(Response.Status.OK).build() : Response.status(500).entity(exceptionMessages).build();


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

