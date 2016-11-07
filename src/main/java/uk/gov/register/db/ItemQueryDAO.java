package uk.gov.register.db;

import org.skife.jdbi.v2.ResultIterator;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.customizers.FetchSize;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import org.skife.jdbi.v2.sqlobject.customizers.SingleValueResult;
import uk.gov.register.core.Item;
import uk.gov.register.db.mappers.ItemMapper;

import java.util.Collection;
import java.util.Optional;

public interface ItemQueryDAO {

    @SqlQuery("select * from item where sha256hex=:sha256hex")
    @SingleValueResult(Item.class)
    @RegisterMapper(ItemMapper.class)
    Optional<Item> getItemBySHA256(@Bind("sha256hex") String sha256Hash);

    //Note: This is fine for small data registers like country
    @SqlQuery("select * from item")
    @RegisterMapper(ItemMapper.class)
    Collection<Item> getAllItemsNoPagination();

    @SqlQuery("select * from item where exists(select 1 from entry where entry_number between :startEntryNo and :endEntryNo)")
    @RegisterMapper(ItemMapper.class)
    @FetchSize(262144)
    ResultIterator<Item> getIterator(@Bind("startEntryNo") int startEntryNo, @Bind("endEntryNo") int endEntryNo);
}
