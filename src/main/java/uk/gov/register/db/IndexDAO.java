package uk.gov.register.db;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.BindBean;
import org.skife.jdbi.v2.sqlobject.SqlBatch;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.BatchChunkSize;
import org.skife.jdbi.v2.sqlobject.customizers.Define;
import org.skife.jdbi.v2.sqlobject.stringtemplate.UseStringTemplate3StatementLocator;
import uk.gov.register.core.EndIndex;
import uk.gov.register.core.StartIndex;

@UseStringTemplate3StatementLocator
public interface IndexDAO {
    @SqlUpdate("insert into \"<schema>\".index (name, key, sha256hex, start_entry_number, start_index_entry_number) values (:name, :key, :sha256hex, :start_entry_number, :start_index_entry_number)")
    void start(@Bind("name") String indexName, @Bind("key") String key, @Bind("sha256hex") String itemHash, @Bind("start_entry_number") int startEntryNumber, @Bind("start_index_entry_number") int startIndexEntryNumber, @Define("schema") String schema );

    @SqlUpdate("update \"<schema>\".index i set end_entry_number = :end_entry_number, end_index_entry_number = :end_index_entry_number from \"<schema>\".<entry_table> e where e.entry_number = i.start_entry_number and e.key = :entryKey and i.name = :name and i.key = :indexKey and i.sha256hex = :sha256hex")
    void end(@Bind("name") String indexName, @Bind("entryKey") String entryKey, @Bind("indexKey") String indexKey, @Bind("sha256hex") String itemHash,
             @Bind("end_entry_number") int endEntryNumber, @Bind("end_index_entry_number") int endIndexEntryNumber, @Define("schema") String schema,
             @Define("entry_table") String entryTable);

    @SqlBatch("insert into \"<schema>\".index (name, key, sha256hex, start_entry_number, start_index_entry_number) values (:indexName, :key, :itemHash, :startEntryNumber, :startIndexEntryNumber)")
    @BatchChunkSize(1000)
    void startInBatch(@BindBean Iterable<StartIndex> indexes, @Define("schema") String schema);

    @SqlBatch("update \"<schema>\".index i set end_entry_number = :endEntryNumber, end_index_entry_number = :endIndexEntryNumber from \"<schema>\".<entry_table> e where e.entry_number = i.start_entry_number and e.entry_number = :entryNumberToEnd and e.key = :entryKey and i.name = :indexName and i.key = :indexKey and i.sha256hex = :itemHash")
    @BatchChunkSize(1000)
    void endInBatch(@BindBean Iterable<EndIndex> indexes, @Define("schema") String schema, @Define("entry_table") String entryTable);
}
