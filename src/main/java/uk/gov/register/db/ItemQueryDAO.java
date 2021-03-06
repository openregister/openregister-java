package uk.gov.register.db;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.customizers.Define;
import org.skife.jdbi.v2.sqlobject.customizers.FetchSize;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import org.skife.jdbi.v2.sqlobject.customizers.SingleValueResult;
import org.skife.jdbi.v2.sqlobject.stringtemplate.UseStringTemplate3StatementLocator;
import uk.gov.register.core.Item;
import uk.gov.register.db.mappers.ItemMapper;

import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;

@UseStringTemplate3StatementLocator
public interface ItemQueryDAO {
    @SqlQuery("select sha256hex, blob_hash, content from \"<schema>\".item where sha256hex=:sha256hex order by sha256hex")
    @SingleValueResult(Item.class)
    @RegisterMapper(ItemMapper.class)
    Optional<Item> getItemBySHA256(@Bind("sha256hex") String sha256Hash, @Define("schema") String schema );

    @SqlQuery("select sha256hex, blob_hash, content from \"<schema>\".item where blob_hash=:blob_hash order by blob_hash")
    @SingleValueResult(Item.class)
    @RegisterMapper(ItemMapper.class)
    Optional<Item> getItemByBlobHash(@Bind("blob_hash") String blobHash, @Define("schema") String schema );

    @SqlQuery("select sha256hex, blob_hash, content from \"<schema>\".item order by sha256hex")
    @RegisterMapper(ItemMapper.class)
    Collection<Item> getAllItemsNoPagination( @Define("schema") String schema );

    @SqlQuery("select sha256hex, blob_hash, content from \"<schema>\".item i where exists(select 1 from \"<schema>\".<entry_table> e where i.sha256hex = e.sha256hex) order by sha256hex")
    @RegisterMapper(ItemMapper.class)
    Collection<Item> getAllItemsNoPagination( @Define("schema") String schema, @Define("entry_table") String entryTable );

    @SqlQuery("select sha256hex, blob_hash, content from \"<schema>\".item i where exists(select 1 from \"<schema>\".entry e where i.blob_hash = e.blob_hash) order by blob_order limit :limit")
    @RegisterMapper(ItemMapper.class)
    Collection<Item> getUserItems(@Bind("limit") int limit, @Define("schema") String schema);

    @SqlQuery("select sha256hex, blob_hash, content from \"<schema>\".item i where exists(select 1 from \"<schema>\".entry e where i.blob_hash = e.blob_hash) and blob_order >= (select blob_order from \"<schema>\".item where blob_hash=:start) order by blob_order limit :limit")
    @RegisterMapper(ItemMapper.class)
    Collection<Item> getUserItemsAfter(@Bind("start") String start, @Bind("limit") int limit, @Define("schema") String schema);

    @SqlQuery("select i.sha256hex, i.blob_hash, i.content from \"<schema>\".item i where exists(select 1 from \"<schema>\".entry e where i.sha256hex = e.sha256hex and e.entry_number > :startEntryNo and e.entry_number \\<= :endEntryNo)")
    @RegisterMapper(ItemMapper.class)
    @FetchSize(10000)
    Iterator<Item> getIterator(@Bind("startEntryNo") int startEntryNo, @Bind("endEntryNo") int endEntryNo, @Define("schema") String schema);

    @SqlQuery("select i.sha256hex, i.blob_hash, i.content from \"<schema>\".item i where exists(select 1 from \"<schema>\".<entry_table> e where i.sha256hex = e.sha256hex)")
    @RegisterMapper(ItemMapper.class)
    @FetchSize(10000)
    Iterator<Item> getIterator(@Define("schema") String schema, @Define("entry_table") String entryTable);

    @SqlQuery("select count(*) from \"<schema>\".item where blob_hash in (select blob_hash from \"<schema>\".entry)")
    int getTotalItems(@Define("schema") String schema);
}
