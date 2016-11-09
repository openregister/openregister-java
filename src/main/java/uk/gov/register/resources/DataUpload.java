package uk.gov.register.resources;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.register.core.Entry;
import uk.gov.register.core.Item;
import uk.gov.register.core.Register;
import uk.gov.register.service.RegisterService;
import uk.gov.register.util.CanonicalJsonValidator;
import uk.gov.register.util.ObjectReconstructor;
import uk.gov.register.views.ViewFactory;

import javax.annotation.security.PermitAll;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import java.time.Instant;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

@Path("/")
public class DataUpload {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    protected final ViewFactory viewFactory;
    private RegisterService registerService;
    private ObjectReconstructor objectReconstructor;
    private CanonicalJsonValidator canonicalizationValidator;

    @Inject
    public DataUpload(ViewFactory viewFactory, RegisterService registerService, ObjectReconstructor objectReconstructor, CanonicalJsonValidator canonicalizationValidator) {
        this.viewFactory = viewFactory;
        this.registerService = registerService;
        this.objectReconstructor = objectReconstructor;
        this.canonicalizationValidator = canonicalizationValidator;
    }

    @Context
    HttpServletRequest httpServletRequest;

    @POST
    @PermitAll
    @Path("/load")
    public void load(String payload) {
        try {
            String[] jsonLines = payload.split("\n");
            mintItems(jsonLines, true);
        } catch (Throwable t) {
            logger.error(Throwables.getStackTraceAsString(t));
            throw t;
        }
    }

    @POST
    @PermitAll
    @Path("/loadCanonicalData")
    public void loadCanonicalData(String payload) {
        try {
            String[] jsonLines = payload.split("\n");
            mintItems(jsonLines, false);
        } catch (Throwable t) {
            logger.error(Throwables.getStackTraceAsString(t));
            throw t;
        }
    }

    private void mintItems(String[] jsonLines, boolean canonicalizeItems) {
        registerService.asAtomicRegisterOperation(register -> {
            Iterable<String> jsonLinesIterable = Arrays.asList(jsonLines);

            if (!canonicalizeItems) {
                // Check that item strings are in canonical form
                jsonLinesIterable.forEach(l -> canonicalizationValidator.validateItemStringIsCanonicalized(l));
            }

            AtomicInteger currentEntryNumber = new AtomicInteger(register.getTotalEntries());
            Function<String, JsonNode> reconstructionFn = canonicalizeItems ? objectReconstructor::reconstruct : objectReconstructor::reconstructWithoutCanonicalization;
            Iterables.transform(jsonLinesIterable, l -> new Item(reconstructionFn.apply(l))).forEach(item -> mintItem(register, currentEntryNumber, item));
        });
    }

    private void mintItem(Register register, AtomicInteger currentEntryNumber, Item item) {
        register.putItem(item);
        register.appendEntry(new Entry(currentEntryNumber.incrementAndGet(), item.getSha256hex(), Instant.now()));
    }
}

