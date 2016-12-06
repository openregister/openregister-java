package uk.gov.register.resources;

import org.flywaydb.core.Flyway;
import org.junit.Test;

import javax.ws.rs.core.Response;

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

