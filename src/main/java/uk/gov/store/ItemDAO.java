package uk.gov.store;

import org.skife.jdbi.v2.sqlobject.BindBean;
import org.skife.jdbi.v2.sqlobject.SqlBatch;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import uk.gov.mint.Item;

public interface ItemDAO {

    @SqlUpdate("create table if not exists item (sha256hex varchar primary key, content bytea)")
    void ensureSchema();

    @SqlBatch("insert into item(sha256hex, content) values(:sha256hex, :canonicalContent) on conflict do nothing")
    void insertInBatch(@BindBean Iterable<Item> items);
}
