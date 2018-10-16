package uk.gov.register.db;

import org.skife.jdbi.v2.sqlobject.SqlBatch;
import org.skife.jdbi.v2.sqlobject.customizers.BatchChunkSize;
import org.skife.jdbi.v2.sqlobject.customizers.Define;
import org.skife.jdbi.v2.sqlobject.stringtemplate.UseStringTemplate3StatementLocator;
import uk.gov.register.core.Blob;
import uk.gov.register.store.postgres.BindItem;

@UseStringTemplate3StatementLocator
public interface BlobDAO {
    @SqlBatch("insert into \"<schema>\".item(sha256hex, content) values(:sha256hex, :content) on conflict do nothing")
    @BatchChunkSize(1000)
    void insertInBatch(@BindItem Iterable<Blob> items, @Define("schema") String schema );
}
