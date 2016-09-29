package uk.gov.register.db;

import org.skife.jdbi.v2.sqlobject.BindBean;
import org.skife.jdbi.v2.sqlobject.SqlBatch;
import uk.gov.register.core.Item;

public interface ItemDAO {
    @SqlBatch("insert into item(sha256hex, content) values(:sha256hex, :contentAsJsonb) on conflict do nothing")
    void insertInBatch(@BindBean Iterable<Item> items);
}
