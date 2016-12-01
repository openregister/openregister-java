package uk.gov.register.resources;

import org.flywaydb.core.Flyway;
import org.junit.Before;
import org.junit.Test;
import uk.gov.register.core.RegisterReadOnly;
import uk.gov.register.views.HomePageView;
import uk.gov.register.views.ViewFactory;

import javax.ws.rs.core.Response;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

public class DeleteRegisterDataResourceTest {

    @Test
    public void shouldUseFlywayToDeleteData() throws Exception {
        Flyway flywayMock = mock(Flyway.class);
        DeleteRegisterDataResource sutResource = new DeleteRegisterDataResource(flywayMock);

        Response response = sutResource.deleteRegisterData();

        assertThat(response.getStatus(), equalTo(200));
        verify(flywayMock, times(1)).clean();
        verify(flywayMock, times(1)).migrate();
    }
}

