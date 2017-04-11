package uk.gov.register.db;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.customizers.OverrideStatementLocatorWith;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import org.skife.jdbi.v2.sqlobject.customizers.SingleValueResult;
import uk.gov.register.core.Entry;
import uk.gov.register.core.Record;
import uk.gov.register.db.mappers.DerivationEntryMapper;
import uk.gov.register.db.mappers.DerivationRecordMapper;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;

@OverrideStatementLocatorWith(SchemaRewriter.class)
public interface IndexQueryDAO {
    @SqlQuery(recordForKeyQuery)
    @SingleValueResult(Record.class)
    @RegisterMapper(DerivationRecordMapper.class)
    Optional<Record> findRecord(@Bind("key") String derivationKey, @Bind("name") String derivationName);

    @SqlQuery(recordQuery)
    @RegisterMapper(DerivationRecordMapper.class)
    List<Record> findRecords(@Bind("limit") int limit, @Bind("offset") int offset, @Bind("name") String derivationName);

    @SqlQuery("select max(r.index_entry_number) from (select greatest(start_index_entry_number, end_index_entry_number) as index_entry_number from :schema.index where name = :name) r")
    int getCurrentIndexEntryNumber(@Bind("name") String indexName);

    @SqlQuery(entriesQuery)
    @RegisterMapper(DerivationEntryMapper.class)
    Iterator<Entry> getIterator(@Bind("name") String indexName);

    @SqlQuery(entriesQueryBetweenEntries)
    @RegisterMapper(DerivationEntryMapper.class)
    Iterator<Entry> getIterator(@Bind("name") String indexName, @Bind("total_entries_1") int totalEntries1,  @Bind("total_entries_2") int totalEntries2);

    @SqlQuery("select count(1) from :schema.index where name = :name and key = :key and sha256hex = :sha256hex and end_entry_number is null")
    int getExistingIndexCountForItem(@Bind("name") String indexName, @Bind("key") String key, @Bind("sha256hex") String sha256hex);

    String recordForKeyQuery = "select " +
            " entry_numbers.mien as index_entry_number, " +
            " entry_numbers.men as entry_number, " +
            " entry.\"timestamp\" as \"timestamp\", " +
            " unended.sha256_arr as sha256_arr, " +
            " unended.content_arr as content_arr, " +
            " :key as key " +
            "from " +
            " ( " +
            "  select " +
            "   greatest( " +
            "    max( start_index_entry_number ), " +
            "    max( end_index_entry_number ) " +
            "   ) as mien, " +
            "   greatest( " +
            "    max( start_entry_number ), " +
            "    max( end_entry_number ) " +
            "   ) as men " +
            "  from " +
            "   :schema.index " +
            "  where " +
            "   name = :name " +
            "   and key = :key " +
            " ) as entry_numbers cross join( " +
            "  select " +
            "   array_remove( " +
            "    array_agg(ix.sha256hex), " +
            "    null " +
            "   ) as sha256_arr, " +
            "   array_remove( " +
            "    array_agg(im.content), " +
            "    null " +
            "   ) as content_arr " +
            "  from " +
            "   :schema.index as ix join :schema.item as im on " +
            "   ix.sha256hex = im.sha256hex " +
            "  where " +
            "   end_index_entry_number is null " +
            "   and ix.name = :name " +
            "   and ix.key = :key " +
            " ) as unended join :schema.entry on " +
            " entry.entry_number = entry_numbers.men";

    String recordQuery = "select " +
            " entry_numbers.key, " +
            " greatest( " +
            "  entry_numbers.meien, " +
            "  entry_numbers.msien " +
            " ) as index_entry_number, " +
            " greatest( " +
            "  entry_numbers.meen, " +
            "  entry_numbers.msen " +
            " ) as entry_number, " +
            " entry.timestamp as timestamp, " +
            " unended.sha256_arr as sha256_arr, " +
            " unended.content_arr as content_arr " +
            "from " +
            " ( " +
            "  select " +
            "   max( start_index_entry_number ) as msien, " +
            "   max( start_entry_number ) as msen, " +
            "   max( end_index_entry_number ) as meien, " +
            "   max( end_entry_number ) as meen, " +
            "   key " +
            "  from " +
            "   :schema.index " +
            "  where " +
            "   name = :name " +
            "  group by " +
            "   key " +
            " ) as entry_numbers join( " +
            "  select " +
            "   array_remove( " +
            "    array_agg(ix.sha256hex), " +
            "    null " +
            "   ) as sha256_arr, " +
            "   array_remove( " +
            "    array_agg(im.content), " +
            "    null " +
            "   ) as content_arr, " +
            "   ix.key " +
            "  from " +
            "   :schema.index as ix join :schema.item as im on " +
            "   ix.sha256hex = im.sha256hex " +
            "  where " +
            "   end_index_entry_number is null " +
            "   and name = :name " +
            "  group by " +
            "   key " +
            " ) as unended on " +
            " unended.key = entry_numbers.key join :schema.entry on " +
            " entry.entry_number = greatest( " +
            "  entry_numbers.meen, " +
            "  entry_numbers.msen " +
            " ) " +
            "order by " +
            " key " +
            " limit :limit offset :offset ";

    String entriesQuery = "select  " +
            "  index_entry.ien as index_entry_number,  " +
            "  index_entry.en as entry_number,  " +
            "  e.timestamp as timestamp,  " +
            "  index_entry.\"key\" as \"key\",  " +
            "  array_remove(array_agg(unended.sha256hex),null) as sha256_arr  " +
            "from  " +
            "  (  " +
            "    select  " +
            "      end_index_entry_number as ien,  " +
            "      end_entry_number as en,  " +
            "      key  " +
            "    from  " +
            "      :schema.index  " +
            "    where  " +
            "      name = :name  " +
            "      and end_index_entry_number is not null  " +
            "  union select  " +
            "      start_index_entry_number as ien,  " +
            "      start_entry_number as en,  " +
            "      key  " +
            "    from  " +
            "      :schema.index  " +
            "    where  " +
            "      name = :name  " +
            "  ) as index_entry join :schema.entry as e on  " +
            "  e.entry_number = index_entry.en left outer join(  " +
            "    select  " +
            "      ix.sha256hex,  " +
            "      ix.start_index_entry_number,  " +
            "      ix.end_index_entry_number,  " +
            "      ix.\"key\"  " +
            "    from  " +
            "      :schema.index as ix " +
            "      where ix.name = :name " +
            "  ) as unended on  " +
            "  unended.key = index_entry.key  " +
            "  and unended.start_index_entry_number <= index_entry.ien  " +
            "  and(  " +
            "    unended.end_index_entry_number is null  " +
            "    or unended.end_index_entry_number > index_entry.ien  " +
            "  )  " +
            "group by  " +
            "  ien,  " +
            "  en,  " +
            "  \"timestamp\",  " +
            "  index_entry.\"key\"  " +
            "order by  " +
            "  ien  " ;

    String entriesQueryBetweenEntries = "select  " +
            "  index_entry.ien as index_entry_number,  " +
            "  index_entry.en as entry_number,  " +
            "  e.timestamp as timestamp,  " +
            "  index_entry.\"key\" as \"key\",  " +
            "  array_remove(array_agg(unended.sha256hex),null) as sha256_arr  " +
            "from  " +
            "  (  " +
            "    select  " +
            "      end_index_entry_number as ien,  " +
            "      end_entry_number as en,  " +
            "      key  " +
            "    from  " +
            "      :schema.index  " +
            "    where  " +
            "      name = :name  " +
            "      and end_index_entry_number is not null and end_entry_number > :total_entries_1 and end_entry_number <= :total_entries_2" +
            "  union select  " +
            "      start_index_entry_number as ien,  " +
            "      start_entry_number as en,  " +
            "      key  " +
            "    from  " +
            "      :schema.index  " +
            "    where  " +
            "      name = :name and start_entry_number > :total_entries_1 and start_entry_number <= :total_entries_2 " +
            "  ) as index_entry join :schema.entry as e on  " +
            "  e.entry_number = index_entry.en left outer join(  " +
            "    select  " +
            "      ix.sha256hex,  " +
            "      ix.start_index_entry_number,  " +
            "      ix.end_index_entry_number,  " +
            "      ix.\"key\"  " +
            "    from  " +
            "      :schema.index as ix" +
            "    where ix.name = :name " +
            "  ) as unended on  " +
            "  unended.key = index_entry.key  " +
            "  and unended.start_index_entry_number <= index_entry.ien  " +
            "  and(  " +
            "    unended.end_index_entry_number is null  " +
            "    or unended.end_index_entry_number > index_entry.ien  " +
            "  )  " +
            "group by  " +
            "  ien,  " +
            "  en,  " +
            "  \"timestamp\",  " +
            "  index_entry.\"key\"  " +
            "order by  " +
            "  ien  ";

}
