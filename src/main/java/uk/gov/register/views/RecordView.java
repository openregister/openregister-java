package uk.gov.register.views;

import com.google.common.collect.ImmutableList;
import uk.gov.register.core.Entry;
import uk.gov.register.core.Field;
import uk.gov.register.core.Record;
import uk.gov.register.exceptions.FieldConversionException;
import uk.gov.register.service.BlobConverter;

import java.util.Map;

public class RecordView extends RecordsView {

    public RecordView(Record record, Map<String,Field> fields, BlobConverter blobConverter) throws FieldConversionException {
        super(ImmutableList.of(record), fields, blobConverter, true, false);
    }

    @SuppressWarnings("unused, used by template")
    public String getEntryKey() {
        return getEntry().getKey();
    }

    private Entry getEntry() {
        return getRecords().keySet().stream().findFirst().get();
    }
}
