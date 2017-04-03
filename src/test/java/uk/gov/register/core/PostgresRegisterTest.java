package uk.gov.register.core;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.register.db.InMemoryStubs.inMemoryEntryLog;
import static uk.gov.register.db.InMemoryStubs.inMemoryItemStore;

import uk.gov.register.configuration.RegisterFieldsConfiguration;
import uk.gov.register.db.DerivationRecordIndex;
import uk.gov.register.db.InMemoryEntryDAO;
import uk.gov.register.db.IndexDAO;
import uk.gov.register.db.IndexQueryDAO;
import uk.gov.register.exceptions.NoSuchFieldException;
import uk.gov.register.service.ItemValidator;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

public class PostgresRegisterTest {
    private final InMemoryEntryDAO entryDAO = new InMemoryEntryDAO(new ArrayList<>());
    private RecordIndex recordIndex;
    private DerivationRecordIndex derivationRecordIndex;
    private ItemValidator itemValidator;
    private IndexDAO indexDAO;
    private IndexQueryDAO indexQueryDAO;
    private RegisterFieldsConfiguration registerFieldsConfiguration;

    @Before
    public void setup() {
        recordIndex = mock(RecordIndex.class);
        derivationRecordIndex = mock(DerivationRecordIndex.class);
        itemValidator = mock(ItemValidator.class);
        indexDAO = mock(IndexDAO.class);
        indexQueryDAO = mock(IndexQueryDAO.class);
        registerFieldsConfiguration = mock(RegisterFieldsConfiguration.class);
    }

    @Test(expected = NoSuchFieldException.class)
    public void findMax100RecordsByKeyValueShouldFailWhenKeyDoesNotExist() {
        PostgresRegister register = new PostgresRegister(registerMetadata("register"),
                registerFieldsConfiguration, inMemoryEntryLog(entryDAO, indexQueryDAO), inMemoryItemStore(itemValidator, entryDAO),
                recordIndex, indexDAO, indexQueryDAO, derivationRecordIndex);
        register.max100RecordsFacetedByKeyValue("citizen-name", "British");
    }

    @Test
    public void findMax100RecordsByKeyValueShouldReturnValueWhenKeyExists() {
        when(registerFieldsConfiguration.containsField("name")).thenReturn(true);
        PostgresRegister register = new PostgresRegister(registerMetadata("register"),
                registerFieldsConfiguration, inMemoryEntryLog(entryDAO, indexQueryDAO), inMemoryItemStore(itemValidator, entryDAO),
                recordIndex, indexDAO, indexQueryDAO, derivationRecordIndex);
        register.max100RecordsFacetedByKeyValue("name", "United Kingdom");
        verify(recordIndex, times(1)).findMax100RecordsByKeyValue("name", "United Kingdom");
    }

    private RegisterMetadata registerMetadata(String registerName) {
        RegisterMetadata mock = mock(RegisterMetadata.class);
        when(mock.getRegisterName()).thenReturn(new RegisterName(registerName));
        return mock;
    }
}
