package uk.gov.indexer.dao;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.postgresql.util.PGobject;
import org.skife.jdbi.v2.TransactionIsolationLevel;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.Transaction;

import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.List;

public abstract class DestinationDBQueryDAO implements DBQueryDAO {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    static final String INDEXED_ENTRIES_TABLE = "ORDERED_ENTRY_INDEX";
    static final String WATERMARK_TABLE = "STREAMED_ENTRIES";

    @SqlUpdate("CREATE TABLE IF NOT EXISTS " + INDEXED_ENTRIES_TABLE + " (ID SERIAL PRIMARY KEY, ENTRY JSONB)")
    public abstract void ensureIndexedEntriesTableExists();

    @SqlUpdate("CREATE TABLE IF NOT EXISTS " + WATERMARK_TABLE + " (ID INTEGER PRIMARY KEY, TIME TIMESTAMP)")
    public abstract void ensureWaterMarkTableExists();

    @SqlUpdate("INSERT INTO " + WATERMARK_TABLE + "(ID) SELECT 0 WHERE NOT EXISTS (SELECT 1 FROM " + WATERMARK_TABLE + ")")
    public abstract void initialiseWaterMarkTableIfRequired();

    @SqlQuery("SELECT ID FROM " + WATERMARK_TABLE)
    public abstract int currentWaterMark();

    @SqlUpdate("INSERT INTO " + INDEXED_ENTRIES_TABLE + "(ENTRY) VALUES(:entry)")
    abstract int write(@Bind("entry") PGobject entry);

    @SqlUpdate("UPDATE " + WATERMARK_TABLE + " SET ID=ID+1, TIME=NOW()")
    abstract int increaseWaterMarkByOne();

    @Transaction(TransactionIsolationLevel.SERIALIZABLE)
    public void writeEntries(List<byte[]> entryRows) {
        for (byte[] entryRow : entryRows) {
            String entry = new String(entryRow, Charset.forName("UTF-8"));
            int result = write(pgObject(entry));
            if (result > 0) {
                increaseWaterMarkByOne();
            }
        }
    }

    private PGobject pgObject(String entry) {
        try {
            PGobject pGobject = new PGobject();
            pGobject.setType("jsonb");
            pGobject.setValue(entry);
            return pGobject;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private JsonNode getJsonNode(String entry) {
        try {
            return objectMapper.readTree(entry);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
