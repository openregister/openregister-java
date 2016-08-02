package uk.gov.register.resources;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Throwables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.register.configuration.RegisterNameConfiguration;
import uk.gov.register.db.EntryStore;
import uk.gov.register.service.ItemValidator;
import uk.gov.register.util.ObjectReconstructor;
import uk.gov.register.views.ViewFactory;

import javax.annotation.security.PermitAll;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

@Path("/")
public class DataUpload {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    protected final ViewFactory viewFactory;
    private String registerPrimaryKey;
    private ObjectReconstructor objectReconstructor;
    private ItemValidator itemValidator;
    private EntryStore entryStore;

    @Inject
    public DataUpload(ViewFactory viewFactory, RegisterNameConfiguration registerNameConfiguration, ObjectReconstructor objectReconstructor, ItemValidator itemValidator, EntryStore entryStore) {
        this.viewFactory = viewFactory;
        this.objectReconstructor = objectReconstructor;
        this.itemValidator = itemValidator;
        this.entryStore = entryStore;
        this.registerPrimaryKey = registerNameConfiguration.getRegister();
    }

    @Context
    HttpServletRequest httpServletRequest;

    @POST
    @PermitAll
    @Path("/load")
    @Consumes(MediaType.APPLICATION_JSON)
    public void load(String payload) {
        try {
            Iterable<JsonNode> objects = objectReconstructor.reconstruct(payload.split("\n"));
            objects.forEach(singleObject -> itemValidator.validateItem(registerPrimaryKey, singleObject));
            entryStore.load(registerPrimaryKey, objects);
        } catch (Throwable t) {
            logger.error(Throwables.getStackTraceAsString(t));
            throw t;
        }
    }
}

