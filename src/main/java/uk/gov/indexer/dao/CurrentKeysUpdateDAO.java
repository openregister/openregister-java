package uk.gov.indexer.dao;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.BindBean;
import org.skife.jdbi.v2.sqlobject.SqlBatch;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.stringtemplate.UseStringTemplate3StatementLocator;
import org.skife.jdbi.v2.unstable.BindIn;

@UseStringTemplate3StatementLocator("/sql/init_records.sql")
interface CurrentKeysUpdateDAO extends DBConnectionDAO {

    String CURRENT_KEYS_TABLE = "current_keys";

    @SqlUpdate
    void ensureRecordTablesInPlace();

    @SqlUpdate("delete from " + CURRENT_KEYS_TABLE + " where key in (<keys>)")
    int removeRecordWithKeys(@BindIn("keys") Iterable<String> allKeys);

    @SqlBatch("insert into " + CURRENT_KEYS_TABLE + "(serial_number, key) values(:serial_number, :key)")
    void writeCurrentKeys(@BindBean Iterable<CurrentKey> values);

    @SqlUpdate("update total_records set count=count+:noOfNewRecords")
    void updateTotalRecords(@Bind("noOfNewRecords") int noOfNewRecords);
}

