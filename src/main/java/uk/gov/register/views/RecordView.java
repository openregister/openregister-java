package uk.gov.register.views;

import com.google.common.collect.ImmutableList;
import uk.gov.register.core.Entry;
import uk.gov.register.core.Field;
import uk.gov.register.core.Record;
import uk.gov.register.exceptions.FieldConversionException;
import uk.gov.register.service.BlobConverter;
import uk.gov.register.views.v1.V1EntryView;

import java.util.Map;

public class RecordView extends RecordsView {

    public RecordView(Record record, Map<String,Field> fields, BlobConverter blobConverter) throws FieldConversionException {
        super(ImmutableList.of(record), fields, blobConverter, true, false);
    }

    @SuppressWarnings("unused, used by template")
    public String getEntryKey() {
        return getEntry().getKey();
    }

    private V1EntryView getEntry() {
        return new V1EntryView(getRecords().keySet().stream().findFirst().get());
    }
}
