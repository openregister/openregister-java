package uk.gov.register.db;

import org.skife.jdbi.v2.sqlobject.BindBean;
import org.skife.jdbi.v2.sqlobject.SqlBatch;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import uk.gov.register.core.Item;

public interface ItemDAO {

    @SqlUpdate("CREATE TABLE IF NOT EXISTS item (sha256hex VARCHAR PRIMARY KEY, content JSONB);" +
            "CREATE INDEX IF NOT EXISTS item_content_gin ON item USING gin(content jsonb_path_ops);")
    void ensureSchema();

    @SqlBatch("insert into item(sha256hex, content) values(:sha256hexDb, :content) on conflict do nothing")
    void insertInBatch(@BindBean Iterable<Item> items);
}
