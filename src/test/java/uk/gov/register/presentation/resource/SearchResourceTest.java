package uk.gov.register.presentation.resource;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.register.presentation.dao.RecentEntryIndexQueryDAO;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.UriInfo;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

//@RunWith(MockitoJUnitRunner.class)
public class SearchResourceTest {
    @Mock
    RecentEntryIndexQueryDAO queryDAO;

    @Mock
    UriInfo uriInfo;

    @Ignore
    public void findByPrimaryKey_throwsBadRequestException_whenSearchedKeyIsNotPrimaryKeyOfRegister() {
        SearchResource resource = new SearchResource(queryDAO);
        resource.uriInfo = uriInfo;

        when(uriInfo.getAbsolutePath().getHost()).thenReturn("localhost");
        try{
            resource.findByPrimaryKey("someOtherKey", "value");
            fail("Must fail");
        }catch(BadRequestException e){
            //success
        }
    }

}
