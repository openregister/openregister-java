package uk.gov.indexer.dao;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.stringtemplate.UseStringTemplate3StatementLocator;
import org.skife.jdbi.v2.unstable.BindIn;

import java.util.Set;

@UseStringTemplate3StatementLocator
interface CurrentKeysUpdateDAO extends DBConnectionDAO {

    String CURRENT_KEYS_TABLE = "current_keys";
    String TOTAL_RECORDS_TABLE = "total_records";
    String TOTAL_RECORDS_FUNCTION = TOTAL_RECORDS_TABLE + "_fn()";
    String TOTAL_RECORDS_TRIGGER = TOTAL_RECORDS_TABLE + "_trigger";

    @SqlUpdate(
            "CREATE TABLE IF NOT EXISTS " + CURRENT_KEYS_TABLE + " (KEY VARCHAR PRIMARY KEY, SERIAL_NUMBER INTEGER UNIQUE);" +

                    "CREATE TABLE IF NOT EXISTS " + TOTAL_RECORDS_TABLE + " (COUNT INTEGER);" +

                    "INSERT INTO " + TOTAL_RECORDS_TABLE + "(COUNT) SELECT 0 WHERE NOT EXISTS (SELECT 1 FROM " + TOTAL_RECORDS_TABLE + ");" +

                    "CREATE OR REPLACE FUNCTION " + TOTAL_RECORDS_FUNCTION + " RETURNS TRIGGER\n" +
                    "AS $$\n" +
                    "BEGIN\n" +
                    "  IF TG_OP = 'INSERT' THEN\n" +
                    "     EXECUTE 'UPDATE " + TOTAL_RECORDS_TABLE + " SET COUNT=COUNT + 1';\n" +
                    "     RETURN NEW;\n" +
                    "  END IF;\n" +
                    "  RETURN NULL;\n" +
                    "  END;\n" +
                    "$$ LANGUAGE plpgsql;" +

                    "DROP TRIGGER IF EXISTS " + TOTAL_RECORDS_TRIGGER + " ON " + CURRENT_KEYS_TABLE + ";" +

                    "CREATE TRIGGER " + TOTAL_RECORDS_TRIGGER + " \n" +
                    " AFTER INSERT ON " + CURRENT_KEYS_TABLE +
                    " FOR EACH ROW EXECUTE PROCEDURE " + TOTAL_RECORDS_FUNCTION + ";"
    )
    void ensureDatabaseEntitiesInPlace();

    @SqlUpdate("UPDATE " + CURRENT_KEYS_TABLE + " SET SERIAL_NUMBER=:serial_number WHERE KEY=:key")
    int updateSerialNumber(@Bind("serial_number") int serial_number, @Bind("key") String key);

    @SqlQuery("SELECT KEY FROM " + CURRENT_KEYS_TABLE + " WHERE KEY IN (<keys>)")
    Set<String> getExistingKeys(@BindIn("keys") Iterable<String> keys);

    @SqlUpdate("INSERT INTO " + CURRENT_KEYS_TABLE + "(SERIAL_NUMBER, KEY) VALUES(:serial_number, :key)")
    void insertNewKey(@Bind("serial_number") int serial_number, @Bind("key") String key);
}

