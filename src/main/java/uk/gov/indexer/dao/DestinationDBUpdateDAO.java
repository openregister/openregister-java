package uk.gov.indexer.dao;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.postgresql.util.PGobject;
import org.skife.jdbi.v2.TransactionIsolationLevel;
import org.skife.jdbi.v2.sqlobject.Transaction;

import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;

public abstract class DestinationDBUpdateDAO implements DBQueryDAO, WatermarkUpdateDao, IndexedEntriesUpdateDao, CurrentKeysUpdateDao, TotalRegisterEntriesUpdateDao {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Transaction(TransactionIsolationLevel.SERIALIZABLE)
    public void writeEntries(String registerName, List<byte[]> entryRows) {
        for (byte[] entryRow : entryRows) {
            String entry = new String(entryRow,  StandardCharsets.UTF_8);

            int result = write(pgObject(entry));

            if (result > 0) {
                increaseTotalEntriesInRegisterCount();
                increaseWaterMarkByOne();

                String key = getKey(registerName, entry);

                int entryID = getCurrentIdForKey(key);

                if (entryID == -1) {
                    writeCurrentKey(currentWaterMark(), key);
                } else {
                    updateCurrentKey(entryID, key);
                }
            } else {
                throw new RuntimeException("Could not write entry: " + entry);
            }
        }
    }

    private String getKey(String registerName, String entry) {
        JsonNode jsonNode = getJsonNode(entry);
        return jsonNode.get("entry").get(registerName).getTextValue();
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
