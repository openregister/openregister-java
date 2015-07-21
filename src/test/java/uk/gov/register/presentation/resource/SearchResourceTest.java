package uk.gov.register.presentation.resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.register.presentation.dao.RecentEntryIndexQueryDAO;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.NotFoundException;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SearchResourceTest {
    @Mock
    RecentEntryIndexQueryDAO queryDAO;

    @Mock
    HttpServletRequest httpServletRequest;

    @Test
    public void findByPrimaryKey_throwsBadRequestException_whenSearchedKeyIsNotPrimaryKeyOfRegister() {
        SearchResource resource = new SearchResource(queryDAO);
        resource.httpServletRequest = httpServletRequest;

        when(httpServletRequest.getRequestURL()).thenReturn(new StringBuffer("http://localhost:9999/someOtherKey/value"));
        try {
            resource.findByPrimaryKey("someOtherKey", "value");
            fail("Must fail");
        } catch (NotFoundException e) {
            //success
        }
    }

}
