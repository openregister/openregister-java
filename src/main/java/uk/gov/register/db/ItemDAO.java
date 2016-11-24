package uk.gov.register.db;

import org.skife.jdbi.v2.sqlobject.SqlBatch;
import uk.gov.register.core.Item;
import uk.gov.register.store.postgres.BindItem;

public interface ItemDAO {
    @SqlBatch("insert into item(sha256hex, content) values(:sha256hex, :content) on conflict do nothing")
    void insertInBatch(@BindItem Iterable<Item> items);
}
