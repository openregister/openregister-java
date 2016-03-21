package uk.gov.indexer.dao;

import org.postgresql.util.PGobject;

import java.sql.SQLException;

@Deprecated
public class OrderedEntryIndex {
    private final int serial_number;
    private final String entry;

    public OrderedEntryIndex(int serial_number, String entry) {
        this.serial_number = serial_number;
        this.entry = entry;
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

    @SuppressWarnings("unused, used by DAO")
    public PGobject getDbEntry() {
        return pgObject(entry);
    }

    public int getSerial_number() {
        return serial_number;
    }

    public String getEntry() {
        return entry;
    }
}
