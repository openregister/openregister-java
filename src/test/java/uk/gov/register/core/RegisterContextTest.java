package uk.gov.register.core;

import org.flywaydb.core.Flyway;
import org.junit.Before;
import org.junit.Test;
import org.skife.jdbi.v2.DBI;
import uk.gov.register.configuration.ConfigManager;
import uk.gov.register.exceptions.NoSuchConfigException;

import java.io.IOException;
import java.util.Optional;

import static org.mockito.Mockito.*;

public class RegisterContextTest {
    private RegisterName registerName;
    private ConfigManager configManager;
    private DBI dbi;
    private Flyway flyway;

    @Before
    public void setup() {
        registerName = new RegisterName("register");
        configManager = mock(ConfigManager.class, RETURNS_DEEP_STUBS);
        dbi = mock(DBI.class);
        flyway = mock(Flyway.class);
    }

    @Test
    public void resetRegister_shouldNotResetRegister_whenEnableRegisterDataDeleteIsDisabled() throws IOException, NoSuchConfigException {
        RegisterContext context = new RegisterContext(registerName, configManager, dbi, flyway, Optional.empty(), false, false);
        context.resetRegister();

        verify(flyway, never()).clean();
        verify(configManager, never()).refreshConfig();
        verify(flyway, never()).migrate();
    }

    @Test
    public void resetRegister_shouldResetRegister_whenEnableRegisterDataDeleteIsEnabled() throws IOException, NoSuchConfigException {
        RegisterContext context = new RegisterContext(registerName, configManager, dbi, flyway, Optional.empty(), true, false);
        context.resetRegister();

        verify(flyway, times(1)).clean();
        verify(configManager, times(1)).refreshConfig();
        verify(flyway, times(1)).migrate();
    }
}