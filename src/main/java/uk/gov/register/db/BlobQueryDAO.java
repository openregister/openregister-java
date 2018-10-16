package uk.gov.register.db;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.customizers.Define;
import org.skife.jdbi.v2.sqlobject.customizers.FetchSize;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import org.skife.jdbi.v2.sqlobject.customizers.SingleValueResult;
import org.skife.jdbi.v2.sqlobject.stringtemplate.UseStringTemplate3StatementLocator;
import uk.gov.register.core.Blob;
import uk.gov.register.db.mappers.BlobMapper;

import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;

@UseStringTemplate3StatementLocator
public interface BlobQueryDAO {
    @SqlQuery("select sha256hex, content from \"<schema>\".item where sha256hex=:sha256hex order by sha256hex")
    @SingleValueResult(Blob.class)
    @RegisterMapper(BlobMapper.class)
    Optional<Blob> getItemBySHA256(@Bind("sha256hex") String sha256Hash, @Define("schema") String schema );

    //Note: This is fine for small data registers like country
    @SqlQuery("select sha256hex, content from \"<schema>\".item order by sha256hex")
    @RegisterMapper(BlobMapper.class)
    Collection<Blob> getAllItemsNoPagination(@Define("schema") String schema );

    @SqlQuery("select i.sha256hex, i.content from \"<schema>\".item i")
    @RegisterMapper(BlobMapper.class)
    @FetchSize(10000)
    Iterator<Blob> getIterator(@Define("schema") String schema);

    @SqlQuery("select i.sha256hex, i.content from \"<schema>\".item i where exists(select 1 from \"<schema>\".entry_item ei where i.sha256hex = ei.sha256hex and ei.entry_number > :startEntryNo and ei.entry_number \\<= :endEntryNo)")
    @RegisterMapper(BlobMapper.class)
    @FetchSize(10000)
    Iterator<Blob> getIterator(@Bind("startEntryNo") int startEntryNo, @Bind("endEntryNo") int endEntryNo, @Define("schema") String schema );

    @SqlQuery("select i.sha256hex, i.content from \"<schema>\".item i where exists(select 1 from \"<schema>\".entry_item_system esi where i.sha256hex = esi.sha256hex)")
    @RegisterMapper(BlobMapper.class)
    @FetchSize(10000)
    Iterator<Blob> getSystemItemIterator(@Define("schema") String schema);
}
