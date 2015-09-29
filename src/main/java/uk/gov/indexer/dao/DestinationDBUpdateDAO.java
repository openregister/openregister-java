package uk.gov.indexer.dao;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.postgresql.util.PGobject;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.TransactionIsolationLevel;
import org.skife.jdbi.v2.sqlobject.Transaction;
import org.skife.jdbi.v2.sqlobject.mixins.GetHandle;

import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;


public abstract class DestinationDBUpdateDAO implements GetHandle, DBConnectionDAO {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final TotalRegisterEntriesUpdateDAO totalRegisterEntriesUpdateDAO;
    private final CurrentKeysUpdateDAO currentKeysUpdateDAO;
    private final IndexedEntriesUpdateDAO indexedEntriesUpdateDAO;

    public DestinationDBUpdateDAO() {
        Handle handle = getHandle();
        totalRegisterEntriesUpdateDAO = handle.attach(TotalRegisterEntriesUpdateDAO.class);

        currentKeysUpdateDAO = handle.attach(CurrentKeysUpdateDAO.class);

        indexedEntriesUpdateDAO = handle.attach(IndexedEntriesUpdateDAO.class);

        indexedEntriesUpdateDAO.ensureIndexedEntriesTableExists();

        currentKeysUpdateDAO.ensureCurrentKeysTableExists();

        totalRegisterEntriesUpdateDAO.ensureTotalEntriesInRegisterTableExists();
        totalRegisterEntriesUpdateDAO.initialiseTotalEntriesInRegisterIfRequired();
    }

    public int lastReadSerialNumber() {
        return indexedEntriesUpdateDAO.lastReadSerialNumber();
    }

    @Transaction(TransactionIsolationLevel.SERIALIZABLE)
    public void writeEntries(String registerName, List<Entry> entries) {
        for (Entry entry : entries) {
            String contents = new String(entry.contents, StandardCharsets.UTF_8);

            int result = indexedEntriesUpdateDAO.write(entry.serial_number, pgObject(contents));

            if (result > 0) {
                totalRegisterEntriesUpdateDAO.increaseTotalEntriesInRegisterCount();

                String key = getKey(registerName, contents);

                if (currentKeysUpdateDAO.doesRecordExistWithKey(key)) {
                    currentKeysUpdateDAO.updateSerialNumber(entry.serial_number, key);
                } else {
                    currentKeysUpdateDAO.writeCurrentKey(entry.serial_number, key);
                }
            } else {
                throw new RuntimeException("Could not write entry: " + contents);
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
