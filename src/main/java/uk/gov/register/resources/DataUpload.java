package uk.gov.register.resources;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.register.core.Entry;
import uk.gov.register.core.Item;
import uk.gov.register.core.Register;
import uk.gov.register.serialization.RegisterComponents;
import uk.gov.register.service.ItemValidator;
import uk.gov.register.service.RegisterService;
import uk.gov.register.service.RegisterUpdateService;
import uk.gov.register.util.ObjectReconstructor;
import uk.gov.register.views.ViewFactory;

import javax.annotation.security.PermitAll;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

@Path("/")
public class DataUpload {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    protected final ViewFactory viewFactory;
    private final RegisterService registerService;
    private final String registerPrimaryKey;
    private final ObjectReconstructor objectReconstructor;
    private final ItemValidator itemValidator;
    private final RegisterUpdateService registerUpdateService;


    @Inject

    public DataUpload(ViewFactory viewFactory, RegisterService registerService, RegisterNameConfiguration registerNameConfiguration, ObjectReconstructor objectReconstructor, ItemValidator itemValidator, RegisterUpdateService registerUpdateService) {
        this.viewFactory = viewFactory;
        this.registerService = registerService;
        this.objectReconstructor = objectReconstructor;
    }

    @Context
    HttpServletRequest httpServletRequest;

    @POST
    @PermitAll
    @Path("/load")
    public void load(String payload) {
        try {
            Iterable<JsonNode> objects = objectReconstructor.reconstruct(payload.split("\n"));
            mintItems(objects);
        } catch (Throwable t) {
            logger.error(Throwables.getStackTraceAsString(t));
            throw t;
        }
    }

    @POST
    @PermitAll
    @Consumes("application/uk-gov-rsf")
    @Path("/load-rsf")
    public void loadRsf(RegisterComponents registerComponents) {
        logger.info("parsed rsf input");
        registerUpdateService.processRegisterComponents(registerComponents);
        logger.info("loading rsf complete");
    }

    private void mintItems(Iterable<JsonNode> objects) {
        registerService.asAtomicRegisterOperation(register -> {
            AtomicInteger currentEntryNumber = new AtomicInteger(register.getTotalEntries());
            Iterables.transform(objects, Item::new).forEach(item -> mintItem(register, currentEntryNumber, item));
        });
    }

    private void mintItem(Register register, AtomicInteger currentEntryNumber, Item item) {
        register.putItem(item);
        register.appendEntry(new Entry(currentEntryNumber.incrementAndGet(), item.getSha256hex(), Instant.now()));
    }
}

