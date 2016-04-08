package uk.gov.indexer.dao;

import org.skife.jdbi.v2.sqlobject.BindBean;
import org.skife.jdbi.v2.sqlobject.SqlBatch;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.stringtemplate.UseStringTemplate3StatementLocator;

@UseStringTemplate3StatementLocator
interface ItemUpdateDAO extends DBConnectionDAO {

    String ITEM_TABLE = "item";

    @SqlUpdate("CREATE TABLE IF NOT EXISTS item (sha256hex VARCHAR PRIMARY KEY, content JSONB)")
    void ensureItemTableInPlace();

    @SqlBatch("insert into " + ITEM_TABLE + "(sha256hex, content) values(:sha256hex, :jsonContent) on conflict do nothing")
    void writeBatch(@BindBean Iterable<Item> items);
}
