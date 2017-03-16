package uk.gov.register.db;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.customizers.FetchSize;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import org.skife.jdbi.v2.sqlobject.customizers.SingleValueResult;
import uk.gov.register.core.Item;
import uk.gov.register.db.mappers.ItemMapper;

import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;

public interface ItemQueryDAO {

    @SqlQuery("select sha256hex, content from item where sha256hex=:sha256hex order by sha256hex")
    @SingleValueResult(Item.class)
    @RegisterMapper(ItemMapper.class)
    Optional<Item> getItemBySHA256(@Bind("sha256hex") String sha256Hash);

    //Note: This is fine for small data registers like country
    @SqlQuery("select sha256hex, content from item order by sha256hex")
    @RegisterMapper(ItemMapper.class)
    Collection<Item> getAllItemsNoPagination();

    @SqlQuery("select i.sha256hex, i.content from item i where exists(select 1 from entry_item ei where i.sha256hex = ei.sha256hex)")
    @RegisterMapper(ItemMapper.class)
    @FetchSize(10000)
    Iterator<Item> getIterator();

    @SqlQuery("select i.sha256hex, i.content from item i where exists(select 1 from entry_item ei where i.sha256hex = ei.sha256hex and ei.entry_number > :startEntryNo and ei.entry_number <= :endEntryNo)")
    @RegisterMapper(ItemMapper.class)
    @FetchSize(10000)
    Iterator<Item> getIterator(@Bind("startEntryNo") int startEntryNo, @Bind("endEntryNo") int endEntryNo);
}
