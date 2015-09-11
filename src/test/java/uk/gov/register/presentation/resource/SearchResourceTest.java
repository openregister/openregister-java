package uk.gov.register.presentation.resource;

import com.google.common.base.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.register.presentation.DbRecord;
import uk.gov.register.presentation.dao.RecentEntryIndexQueryDAO;
import uk.gov.register.presentation.representations.ExtraMediaType;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.lang.reflect.Method;
import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SearchResourceTest {
    @Mock
    RecentEntryIndexQueryDAO queryDAO;

    RequestContext requestContext;

    SearchResource resource;

    @Before
    public void setUp() throws Exception {
        requestContext = new RequestContext(){
            @Override
            public String getRegisterPrimaryKey() {
                return "school";
            }
        };
        resource = new SearchResource(null, requestContext, queryDAO);
    }

    @Test
    public void findByPrimaryKey_throwsNotFoundException_whenSearchedKeyIsNotPrimaryKeyOfRegister() {
        RequestContext requestContext = new RequestContext() {
            @Override
            public String getRegisterPrimaryKey() {
                return "localhost";
            }
        };

        SearchResource resource = new SearchResource(null, requestContext, null);

        try {
            resource.findByPrimaryKey("someOtherKey", "value");
            fail("Must fail");
        } catch (NotFoundException e) {
            //success
        }
    }

    @Test
    public void findByPrimaryKey_throwsNotFoundException_whenSearchedKeyIsNotFound() {
        when(queryDAO.findByKeyValue("school", "value")).thenReturn(Optional.<DbRecord>absent());
        try {
            resource.findByPrimaryKey("school", "value");
            fail("Must fail");
        } catch (NotFoundException e) {
            //success
        }
    }


    @Test
    public void findByHash_throwsNotFoundWhenHashIsNotFound() {
        when(queryDAO.findByHash("123")).thenReturn(Optional.<DbRecord>absent());
        try {
            resource.findByHash("123");
            fail("Must fail");
        } catch (NotFoundException e) {
            //success
        }
    }

    @Test
    public void findByPrimaryKeySupportsTurtleHtmlAndJson() throws Exception {
        Method searchMethod = SearchResource.class.getDeclaredMethod("findByPrimaryKey", String.class, String.class);
        List<String> declaredMediaTypes = asList(searchMethod.getDeclaredAnnotation(Produces.class).value());
        assertThat(declaredMediaTypes,
                hasItems(MediaType.TEXT_HTML,
                        MediaType.APPLICATION_JSON,
                        ExtraMediaType.TEXT_TTL));
    }

    @Test
    public void findByHashSupportsTurtleHtmlAndJson() throws Exception {
        Method findByPrimaryKeyMethod = SearchResource.class.getDeclaredMethod("findByHash", String.class);
        List<String> declaredMediaTypes = asList(findByPrimaryKeyMethod.getDeclaredAnnotation(Produces.class).value());
        assertThat(declaredMediaTypes,
                hasItems(MediaType.TEXT_HTML,
                        MediaType.APPLICATION_JSON,
                        ExtraMediaType.TEXT_TTL));
    }
}
