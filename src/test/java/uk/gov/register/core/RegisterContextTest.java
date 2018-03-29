package uk.gov.register.core;

import org.flywaydb.core.Flyway;
import org.hamcrest.core.Is;
import org.junit.Before;
import org.junit.Test;
import org.skife.jdbi.v2.DBI;
import uk.gov.register.auth.RegisterAuthenticator;
import uk.gov.register.configuration.ConfigManager;
import uk.gov.register.configuration.RegistersConfiguration;
import uk.gov.register.exceptions.NoSuchConfigException;
import uk.gov.register.service.EnvironmentValidator;
import uk.gov.register.service.RegisterLinkService;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.*;

public class RegisterContextTest {
    private RegisterId registerId;
    private ConfigManager configManager;
    private EnvironmentValidator environmentValidator;
    private RegisterLinkService registerLinkService;
    private DBI dbi;
    private Flyway flyway;
    private String schema;

    @Before
    public void setup() {
        registerId = new RegisterId("register");
        schema = "register";
        configManager = mock(ConfigManager.class, RETURNS_DEEP_STUBS);
        environmentValidator = mock(EnvironmentValidator.class);
        registerLinkService = mock(RegisterLinkService.class);
        dbi = mock(DBI.class);
        flyway = mock(Flyway.class);
    }

    @Test
    public void resetRegister_shouldNotResetRegister_whenEnableRegisterDataDeleteIsDisabled() throws IOException, NoSuchConfigException {
        RegisterContext context = new RegisterContext(registerId, configManager, environmentValidator, registerLinkService, dbi, flyway, schema, false, false, emptyList(), new RegisterAuthenticator("", ""));
        context.resetRegister();

        verify(flyway, never()).clean();
        verify(configManager, never()).refreshConfig();
        verify(flyway, never()).migrate();
    }

    @Test
    public void resetRegister_shouldResetRegister_whenEnableRegisterDataDeleteIsEnabled() throws IOException, NoSuchConfigException {
        RegisterContext context = new RegisterContext(registerId, configManager, environmentValidator, registerLinkService, dbi, flyway, schema, true, false, emptyList(), new RegisterAuthenticator("", ""));
        context.resetRegister();

        verify(flyway, times(1)).clean();
        verify(configManager, times(1)).refreshConfig();
        verify(flyway, times(1)).migrate();
    }

    @Test
    public void getRegisterMetadata_returnsUpToDateConfigProvidedByConfigManager() {
        RegisterMetadata expectedInitialMetadata = new RegisterMetadata(
                new RegisterId("test-register-1"),
                Collections.emptyList(),
                "copyright-1",
                "registry-1",
                "text-1",
                "phase-1");

        RegisterMetadata expectedUpdatedMetadata = new RegisterMetadata(
                new RegisterId("test-register-2"),
                Collections.emptyList(),
                "copyright-2",
                "registry-2",
                "text-2",
                "phase-2");


        RegistersConfiguration rcMock = mock(RegistersConfiguration.class);
        when(configManager.getRegistersConfiguration()).thenReturn(rcMock);
        when(rcMock.getRegisterMetadata(registerId))
                .thenReturn(expectedInitialMetadata)
                .thenReturn(expectedUpdatedMetadata);

        RegisterContext context = new RegisterContext(registerId, configManager, environmentValidator, registerLinkService, dbi, flyway, schema, true, false, emptyList(), new RegisterAuthenticator("", ""));

        RegisterMetadata actualInitialMetadata = context.getRegisterMetadata();
        assertThat(actualInitialMetadata, equalTo(expectedInitialMetadata));

        RegisterMetadata actualUpdatedMetadata = context.getRegisterMetadata();
        assertThat(actualUpdatedMetadata, equalTo(expectedUpdatedMetadata));
    }

    @Test
    public void shouldSetUserIndexFunctions(){
        RegisterContext context = new RegisterContext(registerId, configManager, environmentValidator, registerLinkService, dbi, flyway, schema,
                true, false, Arrays.asList("current-countries","local-authority-by-type"), new RegisterAuthenticator("", ""));
        PostgresRegister register = (PostgresRegister)context.buildOnDemandRegister();
        assertThat(register.getIndexFunctionsByEntryType().get(EntryType.user).size(), Is.is(3));
        List<String> indexFunctionNames = register.getIndexFunctionsByEntryType().get(EntryType.user).stream().map(ifn -> ifn.getClass().getName()).collect(toList());
        assertThat(indexFunctionNames, containsInAnyOrder("uk.gov.register.indexer.function.CurrentCountriesIndexFunction", "uk.gov.register.indexer.function.LatestByKeyIndexFunction", "uk.gov.register.indexer.function.LocalAuthorityByTypeIndexFunction"));

    }

    @Test
    public void shouldHandleNoUserIndexFunctions(){
        RegisterContext context = new RegisterContext(registerId, configManager, environmentValidator, registerLinkService, dbi, flyway, schema,
                true, false, Collections.emptyList(), new RegisterAuthenticator("", ""));
        PostgresRegister register = (PostgresRegister)context.buildOnDemandRegister();
        assertThat(register.getIndexFunctionsByEntryType().get(EntryType.user).size(), Is.is(1));
    }

    @Test
    public void shouldSetSystemIndexFunctions(){
        RegisterContext context = new RegisterContext(registerId, configManager, environmentValidator, registerLinkService, dbi, flyway, schema,
                true, false, Arrays.asList("current-countries"), new RegisterAuthenticator("", ""));
        PostgresRegister register = (PostgresRegister)context.buildOnDemandRegister();
        assertThat(register.getIndexFunctionsByEntryType().get(EntryType.system).size(), Is.is(1));
        List<String> indexFunctionNames = register.getIndexFunctionsByEntryType().get(EntryType.system).stream().map(ifn -> ifn.getClass().getName()).collect(toList());
        assertThat(indexFunctionNames, containsInAnyOrder("uk.gov.register.indexer.function.LatestByKeyIndexFunction"));

    }
}
