package uk.gov.register.db;

import uk.gov.register.core.Record;
import uk.gov.register.db.mappers.DerivationRecordMapper;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import org.skife.jdbi.v2.sqlobject.customizers.SingleValueResult;

public interface IndexQueryDAO {

    String recordForKeyQuery = "select " +
            " entry_nums.key, " +
            " max( entry_nums.ien ) as index_entry_number, " +
            " max( entry_nums.en ) as entry_number, " +
            " max( e.\"timestamp\" ) as \"timestamp\", " +
            " array_agg(unended.sha256hex) as sha256_arr, " +
            " array_agg(unended.content) as content_arr " +
            "from " +
            " ( " +
            "  select " +
            "   coalesce( " +
            "    end_index_entry_number, " +
            "    start_index_entry_number " +
            "   ) as ien, " +
            "   coalesce( " +
            "    end_entry_number, " +
            "    start_entry_number " +
            "   ) as en, " +
            "   sha256hex, " +
            "   key " +
            "  from " +
            "   \"index\" " +
            "  where " +
            "   name = :name and key = :key " +
            " ) as entry_nums join entry as e on " +
            " e.entry_number = entry_nums.en  " +
            " left outer join ( " +
            "  select " +
            "   ix.sha256hex, " +
            "   im.content " +
            "  from " +
            "   index as ix join item as im on " +
            "   ix.sha256hex = im.sha256hex " +
            "  where " +
            "   end_index_entry_number is null " +
            " ) as unended on " +
            " unended.sha256hex = entry_nums.sha256hex " +
            "group by " +
            " entry_nums.key " +
            "order by " +
            " entry_nums.key";

    String recordQuery = "select " +
            " entry_nums.key, " +
            " max( entry_nums.ien ) as index_entry_number, " +
            " max( entry_nums.en ) as entry_number, " +
            " max( e.\"timestamp\" ) as \"timestamp\", " +
            " array_agg(unended.sha256hex) as sha256_arr, " +
            " array_agg(unended.content) as content_arr " +
            "from " +
            " ( " +
            "  select " +
            "   coalesce( " +
            "    end_index_entry_number, " +
            "    start_index_entry_number " +
            "   ) as ien, " +
            "   coalesce( " +
            "    end_entry_number, " +
            "    start_entry_number " +
            "   ) as en, " +
            "   sha256hex, " +
            "   key " +
            "  from " +
            "   \"index\" " +
            "  where " +
            "   name = :name" +
            " ) as entry_nums join entry as e on " +
            " e.entry_number = entry_nums.en  " +
            " left outer join ( " +
            "  select " +
            "   ix.sha256hex, " +
            "   im.content " +
            "  from " +
            "   index as ix join item as im on " +
            "   ix.sha256hex = im.sha256hex " +
            "  where " +
            "   end_index_entry_number is null " +
            " ) as unended on " +
            " unended.sha256hex = entry_nums.sha256hex " +
            "group by " +
            " entry_nums.key " +
            "order by " +
            " entry_nums.key";

    String entriesQuery = "select  " +
            "  index_entry.ien as index_entry_number,  " +
            "  index_entry.en as entry_number,  " +
            "  e.\"timestamp\" as \"timestamp\",  " +
            "  index_entry.\"key\" as \"key\",  " +
            "  array_agg(unended.sha256hex) as hash_arr,  " +
            "  array_agg(unended.content) as content_arr  " +
            "from  " +
            "  (  " +
            "    select  " +
            "      end_index_entry_number as ien,  " +
            "      end_entry_number as en,  " +
            "      key  " +
            "    from  " +
            "      \"index\"  " +
            "    where  " +
            "      name = :name  " +
            "      and end_index_entry_number is not null  " +
            "  union select  " +
            "      start_index_entry_number as ien,  " +
            "      start_entry_number as en,  " +
            "      key  " +
            "    from  " +
            "      \"index\"  " +
            "    where  " +
            "      name = 'by-type'  " +
            "  ) as index_entry join entry as e on  " +
            "  e.entry_number = index_entry.en join(  " +
            "    select  " +
            "      ix.sha256hex,  " +
            "      im.content as content,  " +
            "      ix.start_index_entry_number,  " +
            "      ix.end_index_entry_number,  " +
            "      ix.\"key\"  " +
            "    from  " +
            "      index as ix join item as im on  " +
            "      ix.sha256hex = im.sha256hex  " +
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

    @SqlQuery(recordForKeyQuery)
    @SingleValueResult(Record.class)
    @RegisterMapper(DerivationRecordMapper.class)
    Optional<Record> findRecord(@Bind("key") String derivationKey, @Bind("name") String derivationName);

    @SqlQuery(recordQuery)
    @RegisterMapper(DerivationRecordMapper.class)
    List<Record> findRecords(int limit, int offset, @Bind("name") String derivationName);

    @SqlQuery("select max(r.index_entry_number) from (select greatest(start_index_entry_number, end_index_entry_number) as index_entry_number from index where name = :name) r")
    int getCurrentIndexEntryNumber(@Bind("name") String indexName);

    @SqlQuery("select count(1) from index where name = :name and key = :key and sha256hex = :sha256hex and end_entry_number is null")
    int getExistingIndexCountForItem(@Bind("name") String indexName, @Bind("key") String key, @Bind("sha256hex") String sha256hex);

}
