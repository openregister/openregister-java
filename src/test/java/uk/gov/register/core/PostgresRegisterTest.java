package uk.gov.register.core;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.register.configuration.RegisterFieldsConfiguration;
import uk.gov.register.db.DerivationRecordIndex;
import uk.gov.register.db.InMemoryEntryDAO;
import uk.gov.register.db.IndexDAO;
import uk.gov.register.db.IndexQueryDAO;
import uk.gov.register.exceptions.NoSuchFieldException;
import uk.gov.register.indexer.function.IndexFunction;
import uk.gov.register.service.ItemValidator;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;
import static uk.gov.register.db.InMemoryStubs.inMemoryEntryLog;
import static uk.gov.register.db.InMemoryStubs.inMemoryItemStore;

@RunWith(MockitoJUnitRunner.class)
public class PostgresRegisterTest {
    private final InMemoryEntryDAO entryDAO = new InMemoryEntryDAO(new ArrayList<>());
    @Mock
    private RecordIndex recordIndex;
    @Mock
    private DerivationRecordIndex derivationRecordIndex;
    @Mock
    private ItemValidator itemValidator;
    @Mock
    private IndexDAO indexDAO;
    @Mock
    private IndexQueryDAO indexQueryDAO;
    @Mock
    private RegisterFieldsConfiguration registerFieldsConfiguration;
    @Mock
    private List<IndexFunction> indexFunctions;

    private PostgresRegister register;

    @Before
    public void setup() {
        register = new PostgresRegister(registerMetadata("register"), registerFieldsConfiguration,
                inMemoryEntryLog(entryDAO, indexQueryDAO), inMemoryItemStore(itemValidator, entryDAO), recordIndex,
                indexDAO, indexQueryDAO, derivationRecordIndex, indexFunctions);
    }

    @Test(expected = NoSuchFieldException.class)
    public void findMax100RecordsByKeyValueShouldFailWhenKeyDoesNotExist() {
        register.max100RecordsFacetedByKeyValue("citizen-name", "British");
    }

    @Test
    public void findMax100RecordsByKeyValueShouldReturnValueWhenKeyExists() {
        when(registerFieldsConfiguration.containsField("name")).thenReturn(true);
        register.max100RecordsFacetedByKeyValue("name", "United Kingdom");
        verify(recordIndex, times(1)).findMax100RecordsByKeyValue("name", "United Kingdom");
    }

    private RegisterMetadata registerMetadata(String registerName) {
        RegisterMetadata mock = mock(RegisterMetadata.class);
        when(mock.getRegisterName()).thenReturn(new RegisterName(registerName));
        return mock;
    }
}
