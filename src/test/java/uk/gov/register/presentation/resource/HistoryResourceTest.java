package uk.gov.register.presentation.resource;

import org.junit.Test;

import javax.ws.rs.NotFoundException;

import static org.junit.Assert.fail;

public class HistoryResourceTest {
    @Test
    public void history_throwsNotFoundExceptionForNonPrimaryKeyRequests() {
        RequestContext requestContext = new RequestContext() {
            @Override
            public String getRegisterPrimaryKey() {
                return "localhost";
            }
        };

        HistoryResource resource = new HistoryResource(requestContext, null);

        try {
            resource.history("someOtherKey", "value");
            fail("Must fail");
        } catch (NotFoundException e) {
            //success
        }
    }
}
