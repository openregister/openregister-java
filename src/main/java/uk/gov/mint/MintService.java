package uk.gov.mint;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Throwables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.register.configuration.RegisterNameConfiguration;
import uk.gov.store.EntryStore;

import javax.annotation.security.PermitAll;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;

@Path("/")
public class MintService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final String register;
    private final ObjectReconstructor objectReconstructor;
    private final ItemValidator itemValidator;
    private final EntryStore entryStore;

    @Inject
    public MintService(RegisterNameConfiguration register, ObjectReconstructor objectReconstructor, ItemValidator itemValidator, EntryStore entryStore) {
        this.register = register.getRegister();
        this.objectReconstructor = objectReconstructor;
        this.itemValidator = itemValidator;
        this.entryStore = entryStore;
    }

    @Context
    HttpServletRequest httpServletRequest;

    @POST
    @PermitAll
    @Path("/load")
    public void load(String payload) {
        try {
            Iterable<JsonNode> objects = objectReconstructor.reconstruct(payload.split("\n"));
            objects.forEach(singleObject -> itemValidator.validateItem(register, singleObject));
            entryStore.load(register, objects);
        } catch (Throwable t) {
            logger.error(Throwables.getStackTraceAsString(t));
            throw t;
        }
    }
}
