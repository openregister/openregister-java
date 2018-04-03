package uk.gov.register.resources;

import org.junit.Before;
import org.junit.Test;
import uk.gov.register.core.RegisterReadOnly;
import uk.gov.register.views.HomePageView;
import uk.gov.register.views.ViewFactory;

import java.time.Instant;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

public class HomePageResourceTest {

    private RegisterReadOnly registerMock;
    private ViewFactory viewFactoryMock;

    @Before
    public void beforeEach() {
        registerMock = mock(RegisterReadOnly.class);
        viewFactoryMock = mock(ViewFactory.class);
    }
}
