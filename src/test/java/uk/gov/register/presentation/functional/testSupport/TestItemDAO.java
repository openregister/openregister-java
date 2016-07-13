package uk.gov.register.presentation.functional.testSupport;

import org.postgresql.util.PGobject;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;

import java.sql.SQLException;

public abstract class TestItemDAO {
    @SqlUpdate("delete from item where sha256hex=:sha256hex; insert into item(sha256hex, content) values(:sha256hex, :content)")
    public abstract void __insertIfNotExist(@Bind("sha256hex") String sha256, @Bind("content") PGobject item);

    public void insertIfNotExist(String sha256, String item) {
        __insertIfNotExist(sha256, pgObject(item));
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
