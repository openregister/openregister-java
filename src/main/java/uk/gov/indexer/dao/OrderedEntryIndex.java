package uk.gov.indexer.dao;

import org.postgresql.util.PGobject;

import java.sql.SQLException;

public class OrderedEntryIndex {
    private final int serial_number;
    private final String entry;
    private final String leafInput;

    public OrderedEntryIndex(int serial_number, String entry, String leafInput) {
        this.serial_number = serial_number;
        this.entry = entry;
        this.leafInput = leafInput;
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

    @SuppressWarnings("unused, used by DAO")
    public String getLeafInput() {
        return leafInput;
    }

    public int getSerial_number() {
        return serial_number;
    }

    public String getEntry() {
        return entry;
    }
}
