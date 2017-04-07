package uk.gov.register.resources;

import org.junit.Before;
import org.junit.Test;
import uk.gov.register.configuration.IndexConfiguration;
import uk.gov.register.core.RegisterReadOnly;
import uk.gov.register.views.ViewFactory;

import javax.inject.Provider;
import javax.ws.rs.NotFoundException;

import java.util.Arrays;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DerivationIndexResourceTest {
    private DerivationRecordResource resource;

    @Before
    public void setup() {
        RegisterReadOnly register = mock(RegisterReadOnly.class);
        ViewFactory viewFactory = mock(ViewFactory.class);
        RequestContext requestContext = mock(RequestContext.class);
        IndexConfiguration indexConfiguration = mock(IndexConfiguration.class);
        when(indexConfiguration.getIndexes()).thenReturn(Arrays.asList("current-countries"));
        Provider<IndexConfiguration> provider = mock(Provider.class);
        when(provider.get()).thenReturn(indexConfiguration);

        resource = new DerivationRecordResource(register, viewFactory, requestContext, provider);
    }

    @Test(expected = NotFoundException.class)
    public void ensureIndexIsAccessible_shouldThrow404_whenRequestedIndexIsNotFoundInRegister() {
        resource.ensureIndexIsAccessible("local-authority-by-type");
    }

    @Test
    public void ensureIndexIsAccessible_shouldDoNothing_whenRequestedIndexIsFoundInRegister() {
        resource.ensureIndexIsAccessible("current-countries");
    }
}
