package uk.gov.indexer;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.indexer.DestinationPostgresDB;
import uk.gov.indexer.IndexerTask;
import uk.gov.indexer.SourcePostgresDB;

import java.sql.ResultSet;
import java.sql.SQLException;

import static org.mockito.Mockito.*;


@RunWith(MockitoJUnitRunner.class)
public class IndexerTaskTest {
    @Mock
    SourcePostgresDB sourceDB;

    @Mock
    DestinationPostgresDB destinationDB;

    @Test
    public void update_copiesAllEntriesFromSourceDBToDestinationDB_fetchedByWaterMark() throws SQLException {

        IndexerTask indexerTask = new IndexerTask("register", sourceDB, destinationDB);

        ResultSet resultSet = mock(ResultSet.class);
        int currentWaterMark = 0;
        when(destinationDB.currentWaterMark()).thenReturn(currentWaterMark);
        when(sourceDB.read(currentWaterMark)).thenReturn(resultSet);

        indexerTask.update();

        verify(destinationDB).write(resultSet);
    }
}
