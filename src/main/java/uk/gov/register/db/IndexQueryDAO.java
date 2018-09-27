package uk.gov.register.db;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.customizers.Define;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import org.skife.jdbi.v2.sqlobject.stringtemplate.UseStringTemplate3StatementLocator;
import uk.gov.register.core.StartIndex;
import uk.gov.register.db.mappers.*;
import uk.gov.register.indexer.IndexEntryNumberItemCountPair;

import java.util.List;

@UseStringTemplate3StatementLocator
public abstract class IndexQueryDAO {
    @SqlQuery("select max(r.index_entry_number) from (select greatest(start_index_entry_number, end_index_entry_number) as index_entry_number from \"<schema>\".index where name = :name) r")
    public abstract int getCurrentIndexEntryNumber(@Bind("name") String indexName, @Define("schema") String schema);
    
    @SqlQuery("select * from \"<schema>\".index where name = :name and key = :key and sha256hex = :sha256hex and end_entry_number is null order by start_index_entry_number asc")
    @RegisterMapper(StartIndexMapper.class)
    public abstract List<StartIndex> getCurrentStartIndexesForKey(@Bind("name") String indexName, @Bind("key") String key, @Bind("sha256hex") String sha256hex, @Define("schema") String schema);

    @RegisterMapper(IndexItemInfoMapper.class)
    @SqlQuery("select min(i.start_index_entry_number) start_index_entry_number, count(*) existing_item_count from \"<schema>\".index i where i.name = :name and i.key = :key and i.sha256hex = :sha256hex and i.end_entry_number is null")
    public abstract IndexEntryNumberItemCountPair getStartIndexEntryNumberAndExistingItemCount(@Bind("name") String indexName, @Bind("key") String key, @Bind("sha256hex") String sha256hex, @Define("schema") String schema);
}
