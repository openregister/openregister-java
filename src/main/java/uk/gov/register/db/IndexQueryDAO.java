package uk.gov.register.db;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import org.postgresql.util.PGobject;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.customizers.Define;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import org.skife.jdbi.v2.sqlobject.customizers.SingleValueResult;
import org.skife.jdbi.v2.sqlobject.stringtemplate.UseStringTemplate3StatementLocator;
import uk.gov.register.core.Entry;
import uk.gov.register.core.Record;
import uk.gov.register.db.mappers.DerivationEntryMapper;
import uk.gov.register.db.mappers.DerivationRecordMapper;
import uk.gov.register.db.mappers.IndexItemInfoMapper;
import uk.gov.register.db.mappers.RecordMapper;
import uk.gov.register.indexer.IndexEntryNumberItemCountPair;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

@UseStringTemplate3StatementLocator
public abstract class IndexQueryDAO {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    @SqlQuery(recordForKeyQuery)
    @SingleValueResult(Record.class)
    @RegisterMapper(DerivationRecordMapper.class)
    public abstract Optional<Record> findRecord(@Bind("key") String derivationKey, @Bind("name") String derivationName, @Define("schema") String schema, @Define("entry_table") String entryTable);

    @SqlQuery(recordQuery)
    @RegisterMapper(DerivationRecordMapper.class)
    public abstract List<Record> findRecords(@Bind("limit") int limit, @Bind("offset") int offset, @Bind("name") String derivationName, @Define("schema") String schema, @Define("entry_table") String entryTable);

    @SqlQuery("select max(r.index_entry_number) from (select greatest(start_index_entry_number, end_index_entry_number) as index_entry_number from \"<schema>\".index where name = :name) r")
    public abstract int getCurrentIndexEntryNumber(@Bind("name") String indexName, @Define("schema") String schema);

    @SqlQuery(entriesQuery)
    @RegisterMapper(DerivationEntryMapper.class)
    public abstract Iterator<Entry> getIterator(@Bind("name") String indexName, @Define("schema") String schema, @Define("entry_table") String entryTable);

    @SqlQuery(entriesQueryBetweenEntries)
    @RegisterMapper(DerivationEntryMapper.class)
    public abstract Iterator<Entry> getIterator(@Bind("name") String indexName, @Bind("total_entries_1") int totalEntries1, @Bind("total_entries_2") int totalEntries2, @Define("schema") String schema, @Define("entry_table") String entryTable);

    @RegisterMapper(IndexItemInfoMapper.class)
    @SqlQuery("select min(i.start_index_entry_number) start_index_entry_number, count(*) existing_item_count from \"<schema>\".index i where i.name = :name and i.key = :key and i.sha256hex = :sha256hex and i.end_entry_number is null")
    public abstract IndexEntryNumberItemCountPair getStartIndexEntryNumberAndExistingItemCount(@Bind("name") String indexName, @Bind("key") String key, @Bind("sha256hex") String sha256hex, @Define("schema") String schema);

    @SqlQuery(recordCountQuery)
    public abstract int getTotalRecords(@Bind("name") String indexName, @Define("schema") String schema);

    @SqlQuery(recordsByKeyValue)
    @RegisterMapper(RecordMapper.class)
    public abstract List<Record> __findMax100RecordsByKeyValue(@Bind("contentPGobject") PGobject content, @Bind("key") String key, @Define("schema") String schema);

    public List<Record> findMax100RecordsByKeyValue(String key, String value, String schema) {
        return __findMax100RecordsByKeyValue(writePGObject(key, value), key, schema);
    }

    private PGobject writePGObject(String key, String value) {
        try {
            PGobject json = new PGobject();
            json.setType("jsonb");
            json.setValue(objectMapper.writeValueAsString(ImmutableMap.of(key, value)));
            return json;
        } catch (SQLException | JsonProcessingException e) {
            throw Throwables.propagate(e);
        }
    }

    static final String recordsByKeyValue = "select " +
            "e.entry_number, " +
            "array_agg(ei.sha256hex) as sha256hex, " +
            "e.timestamp, " +
            "e.type, " +
            "e.key, " +
            "array_agg(i.content) as content " +
            "from \"<schema>\".entry e " +
            "join \"<schema>\".entry_item ei on ei.entry_number = e.entry_number " +
            "join \"<schema>\".item i on i.sha256hex = ei.sha256hex " +
            "join \"<schema>\".index idx on idx.sha256hex = i.sha256hex " +
            "where i.content @> :contentPGobject " +
            "and idx.name = 'records' " +
            "and idx.end_index_entry_number is null " +
            "group by e.entry_number " +
            "limit 100";

    static final String recordForKeyQuery = "select " +
            "    unended.key, " +
            "    entry_numbers.entry_number, " +
            "    entry_numbers.index_entry_number, " +
            "    e.timestamp, " +
            "    e.type, " +
            "    unended.sha256_arr, " +
            "    unended.content_arr  " +
            "from " +
            "    (select " +
            "        array_remove(array_agg(ix.sha256hex), null) as sha256_arr, " +
            "        array_remove(array_agg(im.content), null) as content_arr, " +
            "        ix.key " +
            "    from \"<schema>\".index as ix " +
            "    join \"<schema>\".item as im on ix.sha256hex = im.sha256hex " +
            "    where " +
            "        end_index_entry_number is null " +
            "        and name = :name and key = :key group by key) " +
            "as unended " +
            "join " +
            "    (select " +
            "        key, " +
            "        min(entry_number) as entry_number, " +
            "        max_index_entry_number as index_entry_number " +
            "    from " +
            "        (select " +
            "            key, " +
            "            entry_number, " +
            "            index_entry_number, " +
            "            max(index_entry_number) over (partition by key) as max_index_entry_number " +
            "        from " +
            "            (select " +
            "                key, " +
            "                start_entry_number as entry_number, " +
            "                start_index_entry_number as index_entry_number " +
            "            from \"<schema>\".index " +
            "            where name = :name and key = :key " +
            "            union " +
            "            select " +
            "                key, " +
            "                end_entry_number as entry_number, " +
            "                end_index_entry_number as index_entry_number " +
            "            from \"<schema>\".index " +
            "            where name = :name and key = :key and end_entry_number is not null) " +
            "        as all_entries) " +
            "    as with_index_entry_numbers " +
            "    where index_entry_number = max_index_entry_number " +
            "    group by max_index_entry_number, key) " +
            "as entry_numbers " +
            "on unended.key = entry_numbers.key " +
            "join \"<schema>\".<entry_table> e on e.entry_number = entry_numbers.entry_number;";

    static final String recordQuery =
            "select " +
            "    unended.key, " +
            "    entry_numbers.entry_number, " +
            "    entry_numbers.index_entry_number, " +
            "    e.timestamp, " +
            "    e.type, " +
            "    unended.sha256_arr, " +
            "    unended.content_arr  " +
            "from " +
            "    (select " +
            "        array_remove(array_agg(ix.sha256hex), null) as sha256_arr, " +
            "        array_remove(array_agg(im.content), null) as content_arr, " +
            "        ix.key " +
            "    from \"<schema>\".index as ix " +
            "    join \"<schema>\".item as im on ix.sha256hex = im.sha256hex " +
            "    where " +
            "        end_index_entry_number is null " +
            "        and name = :name group by key) " +
            "as unended " +
            "join " +
            "    (select " +
            "        key, " +
            "        min(entry_number) as entry_number, " +
            "        max_index_entry_number as index_entry_number " +
            "    from " +
            "        (select " +
            "            key, " +
            "            entry_number, " +
            "            index_entry_number, " +
            "            max(index_entry_number) over (partition by key) as max_index_entry_number " +
            "        from " +
            "            (select " +
            "                key, " +
            "                start_entry_number as entry_number, " +
            "                start_index_entry_number as index_entry_number " +
            "            from \"<schema>\".index " +
            "            where name = :name " +
            "            union " +
            "            select " +
            "                key, " +
            "                end_entry_number as entry_number, " +
            "                end_index_entry_number as index_entry_number " +
            "            from \"<schema>\".index " +
            "            where name = :name and end_entry_number is not null) " +
            "        as all_entries) " +
            "    as with_index_entry_numbers " +
            "    where index_entry_number = max_index_entry_number " +
            "    group by max_index_entry_number, key) " +
            "as entry_numbers " +
            "on unended.key = entry_numbers.key " +
            "join \"<schema>\".<entry_table> e on e.entry_number = entry_numbers.entry_number " +
            "order by e.entry_number desc, entry_numbers.index_entry_number desc " +
           "limit :limit offset :offset;";

    static final String entriesQuery = "select  " +
            "  index_entry.ien as index_entry_number,  " +
            "  index_entry.en as entry_number,  " +
            "  e.timestamp as timestamp,  " +
            "  e.type, " +
            "  index_entry.\"key\" as \"key\",  " +
            "  array_remove(array_agg(unended.sha256hex),null) as sha256_arr  " +
            "from  " +
            "  ( select distinct on (n.ien, n.key) n.ien, n.key, n.en from " +
            "       (" +
            "         select  " +
            "           end_index_entry_number as ien,  " +
            "           end_entry_number as en,  " +
            "           key  " +
            "         from  " +
            "           \"<schema>\".index  " +
            "         where  " +
            "           name = :name  " +
            "           and end_index_entry_number is not null  " +
            "       union select  " +
            "           start_index_entry_number as ien,  " +
            "           start_entry_number as en,  " +
            "           key  " +
            "         from  " +
            "           \"<schema>\".index  " +
            "         where  " +
            "           name = :name  " +
            "       ) n order by n.ien, n.key, n.en " +
            "  ) as index_entry join \"<schema>\".<entry_table> as e on  " +
            "  e.entry_number = index_entry.en left outer join(  " +
            "    select  " +
            "      ix.sha256hex,  " +
            "      ix.start_index_entry_number,  " +
            "      ix.end_index_entry_number,  " +
            "      ix.\"key\"  " +
            "    from  " +
            "      \"<schema>\".index as ix " +
            "      where ix.name = :name " +
            "  ) as unended on  " +
            "  unended.key = index_entry.key  " +
            "  and unended.start_index_entry_number \\<= index_entry.ien  " +
            "  and(  " +
            "    unended.end_index_entry_number is null  " +
            "    or unended.end_index_entry_number > index_entry.ien  " +
            "  )  " +
            "group by  " +
            "  ien,  " +
            "  en,  " +
            "  \"timestamp\",  " +
            "  type, " +
            "  index_entry.\"key\"  " +
            "order by  " +
            "  ien  ";

    static final String entriesQueryBetweenEntries = "select  " +
            "  index_entry.ien as index_entry_number,  " +
            "  index_entry.en as entry_number,  " +
            "  e.timestamp as timestamp,  " +
            "  e.type, " +
            "  index_entry.\"key\" as \"key\",  " +
            "  array_remove(array_agg(unended.sha256hex),null) as sha256_arr  " +
            "from  " +
            "  ( select distinct on (n.ien, n.key) n.ien, n.key, n.en from " +
            "       (" +
            "          select  " +
            "               end_index_entry_number as ien,  " +
            "               end_entry_number as en,  " +
            "               key  " +
            "           from  " +
            "               \"<schema>\".index  " +
            "           where  " +
            "               name = :name  " +
            "               and end_index_entry_number is not null and end_entry_number > :total_entries_1 and end_entry_number \\<= :total_entries_2" +
            "           union select  " +
            "               start_index_entry_number as ien,  " +
            "               start_entry_number as en,  " +
            "               key  " +
            "           from  " +
            "               \"<schema>\".index  " +
            "           where  " +
            "               name = :name " +
            "               and start_index_entry_number is not null and start_entry_number > :total_entries_1 and start_entry_number \\<= :total_entries_2 " +
            "       ) n order by n.ien, n.key, n.en " +
            "  ) as index_entry join \"<schema>\".<entry_table> as e on  " +
            "  e.entry_number = index_entry.en left outer join(  " +
            "    select  " +
            "      ix.sha256hex,  " +
            "      ix.start_index_entry_number,  " +
            "      ix.end_index_entry_number,  " +
            "      ix.\"key\"  " +
            "    from  " +
            "      \"<schema>\".index as ix" +
            "    where ix.name = :name " +
            "  ) as unended on  " +
            "  unended.key = index_entry.key  " +
            "  and unended.start_index_entry_number \\<= index_entry.ien  " +
            "  and(  " +
            "    unended.end_index_entry_number is null  " +
            "    or unended.end_index_entry_number > index_entry.ien  " +
            "  )  " +
            "group by  " +
            "  ien,  " +
            "  en,  " +
            "  \"timestamp\",  " +
            "  type, " +
            "  index_entry.\"key\"  " +
            "order by  " +
            "  ien  ";

    static final String recordCountQuery = "select " +
            "count( distinct key) " +
            "from " +
            " \"<schema>\".index " +
            "where " +
            " name = :name" +
            " and end_index_entry_number is null";


}
