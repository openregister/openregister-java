package uk.gov.register.presentation.functional.testSupport;

import org.postgresql.util.PGobject;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;

import java.sql.SQLException;

public abstract class TestOrderedEntryIndexDAO {
    @SqlUpdate("drop table if exists ordered_entry_index")
    public abstract void dropTable();

    @SqlUpdate("create table if not exists ordered_entry_index (serial_number integer primary key, entry jsonb)")
    public abstract void createTable();

    @SqlUpdate("insert into ordered_entry_index(serial_number,entry) values(:serial_number,:entry)")
    public abstract void __insert(@Bind("serial_number") int serialNumber, @Bind("entry") PGobject entry);

    void insert(int serialNumber, String entry) {
        __insert(serialNumber, pgObject(entry));
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
}

