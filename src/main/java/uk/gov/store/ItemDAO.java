package uk.gov.store;

import org.skife.jdbi.v2.sqlobject.BindBean;
import org.skife.jdbi.v2.sqlobject.SqlBatch;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.stringtemplate.UseStringTemplate3StatementLocator;
import org.skife.jdbi.v2.unstable.BindIn;
import uk.gov.mint.Item;

import java.util.List;

@UseStringTemplate3StatementLocator("/sql/item.sql")
interface ItemDAO {

    @SqlUpdate("create table if not exists item (sha256hex varchar primary key, content bytea)")
    void ensureSchema();

    @SqlBatch("insert into item(sha256hex, content) values(:sha256hex, :canonicalContent)")
    void insertInBatch(@BindBean Iterable<Item> items);

    @SqlQuery
    List<String> existingItemHex(@BindIn("listOfHexValues") Iterable<String> listOfHexValues);
}
