package uk.gov.register.presentation.resource;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import thymeleaf.ThymeleafView;
import uk.gov.register.presentation.Record;
import uk.gov.register.presentation.RegisterNameExtractor;
import uk.gov.register.presentation.representations.ListResultJsonSerializer;
import uk.gov.register.presentation.representations.SingleResultJsonSerializer;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Context;
import java.util.List;

public abstract class ResourceBase {
    public static final int ENTRY_LIMIT = 100;
    @Context
    protected HttpServletRequest httpServletRequest;

    @Context
    protected HttpServletResponse httpServletResponse;

    @Context
    protected ServletContext servletContext;

    protected String getRegisterPrimaryKey() {
        return RegisterNameExtractor.extractRegisterName(httpServletRequest.getHeader("Host"));
    }

    @JsonSerialize(using = SingleResultJsonSerializer.class)
    public class SingleResultView extends ThymeleafView {
        private final Record record;

        public SingleResultView(String templateName, Record record) {
            super(httpServletRequest, httpServletResponse, servletContext, templateName);
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
            super(httpServletRequest, httpServletResponse, servletContext, templateName);
            this.records = records;
        }

        public List<Record> getRecords() {
            return records;
        }

    }

}

