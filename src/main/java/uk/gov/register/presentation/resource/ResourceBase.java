package uk.gov.register.presentation.resource;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import uk.gov.register.presentation.Record;
import uk.gov.register.presentation.representations.ListResultJsonSerializer;
import uk.gov.register.presentation.representations.SingleResultJsonSerializer;
import uk.gov.register.thymeleaf.ThymeleafView;

import java.util.List;

public abstract class ResourceBase {
    public static final int ENTRY_LIMIT = 100;

    protected final RequestContext requestContext;

    public ResourceBase(RequestContext requestContext) {
        this.requestContext = requestContext;
    }

    @JsonSerialize(using = SingleResultJsonSerializer.class)
    public class SingleResultView extends ThymeleafView {
        private final Record record;

        public SingleResultView(Record record) {
            super(requestContext, "entry.html");
            this.record = record;
        }

        public Record getRecord() {
            return record;
        }
    }

    @JsonSerialize(using = ListResultJsonSerializer.class)
    public class ListResultView extends ThymeleafView {
        private final List<Record> records;

        public ListResultView(String templateName, List<Record> records) {
            super(requestContext, templateName);
            this.records = records;
        }

        public List<Record> getRecords() {
            return records;
        }

    }

}

