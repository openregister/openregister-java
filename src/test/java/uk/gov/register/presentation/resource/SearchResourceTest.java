package uk.gov.register.presentation.resource;

import com.google.common.base.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.register.presentation.Entry;
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
    public void findByPrimaryKey_throwsNotFoundException_whenSearchedKeyIsNotPrimaryKeyOfRegister() {
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

    @Test
    public void findByPrimaryKey_throwsNotFoundException_whenSearchedKeyIsNotFound() {
        SearchResource resource = new SearchResource(queryDAO);
        resource.httpServletRequest = httpServletRequest;

        when(httpServletRequest.getRequestURL()).thenReturn(new StringBuffer("http://school.openregister.org/school/value"));
        when(queryDAO.findByKeyValue("school", "value")).thenReturn(Optional.<Entry>absent());
        try {
            resource.findByPrimaryKey("school", "value");
            fail("Must fail");
        } catch (NotFoundException e) {
            //success
        }
    }


    @Test
    public void findByHash_throwsNotFoundWhenHashIsNotFound() {
        SearchResource resource = new SearchResource(queryDAO);
        resource.httpServletRequest = httpServletRequest;

        when(httpServletRequest.getRequestURL()).thenReturn(new StringBuffer("http://school.openregister.org/hash/123"));
        when(queryDAO.findByHash("123")).thenReturn(Optional.<Entry>absent());
        try {
            resource.findByHash("123");
            fail("Must fail");
        } catch (NotFoundException e) {
            //success
        }
    }
}
