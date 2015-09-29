package uk.gov.indexer.dao;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.postgresql.util.PGobject;
import org.skife.jdbi.v2.TransactionIsolationLevel;
import org.skife.jdbi.v2.sqlobject.Transaction;

import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;

public abstract class DestinationDBUpdateDAO implements DBQueryDAO, IndexedEntriesUpdateDAO, CurrentKeysUpdateDAO, TotalRegisterEntriesUpdateDAO {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Transaction(TransactionIsolationLevel.SERIALIZABLE)
    public void writeEntries(String registerName, List<Entry> entries) {
        for (Entry entry : entries) {
            String contents = new String(entry.contents, StandardCharsets.UTF_8);

            int result = write(entry.serial_number, pgObject(contents));

            if (result > 0) {
                increaseTotalEntriesInRegisterCount();

                String key = getKey(registerName, contents);

                if (doesRecordExistsWithKey(key)) {
                    updateSerialNumber(entry.serial_number, key);
                } else {
                    writeCurrentKey(entry.serial_number, key);
                }
            } else {
                throw new RuntimeException("Could not write entry: " + contents);
            }
        }
    }

    public void ensureAllTablesExist() {
        ensureIndexedEntriesTableExists();

        ensureCurrentKeysTableExists();

        ensureTotalEntriesInRegisterTableExists();
        initialiseTotalEntriesInRegisterIfRequired();
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
