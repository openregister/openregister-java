package uk.gov.register.presentation.resource;

import com.fasterxml.jackson.core.JsonProcessingException;
import uk.gov.register.presentation.dao.SignedTreeHeadQueryDAO;
import uk.gov.register.thymeleaf.RegisterProofView;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;


@Path("/")
public class RegisterProofResource {
    private SignedTreeHeadQueryDAO signedTreeHeadQueryDAO;

    @Inject
    public RegisterProofResource(SignedTreeHeadQueryDAO signedTreeHeadQueryDAO) {
        this.signedTreeHeadQueryDAO = signedTreeHeadQueryDAO;
    }

    @GET
    @Path("/register/proof")
    @Produces(MediaType.APPLICATION_JSON)
    public RegisterProofView getRegisterDetails() throws JsonProcessingException {
        return new RegisterProofView(signedTreeHeadQueryDAO.get());
    }
}
