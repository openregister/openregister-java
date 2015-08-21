package uk.gov;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.sql.ResultSet;
import java.sql.SQLException;

import static org.mockito.Mockito.*;


@RunWith(MockitoJUnitRunner.class)
public class IndexerTest {
    @Mock
    SourcePostgresDB sourceDB;

    @Mock
    DestinationPostgresDB destinationDB;

    @Test
    public void update_copiesAllEntriesFromSourceDBToDestinationDB_fetchedByWaterMark() throws SQLException {

        Indexer indexer = new Indexer(sourceDB, destinationDB);

        ResultSet resultSet = mock(ResultSet.class);
        int currentWaterMark = 0;
        when(destinationDB.currentWaterMark()).thenReturn(currentWaterMark);
        when(sourceDB.read(currentWaterMark)).thenReturn(resultSet);

        indexer.update();

        verify(destinationDB).write(resultSet);
    }
}
