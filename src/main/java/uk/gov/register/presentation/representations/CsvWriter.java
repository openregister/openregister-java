package uk.gov.register.presentation.representations;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Iterators;
import org.apache.commons.lang3.StringEscapeUtils;
import uk.gov.register.presentation.Record;
import uk.gov.register.presentation.mapper.JsonObjectMapper;
import uk.gov.register.presentation.view.ListResultView;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Produces(ExtraMediaType.TEXT_CSV)
public class CsvWriter extends RepresentationWriter<ListResultView> {
    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return ListResultView.class.isAssignableFrom(type);
    }

    @Override
    public void writeTo(ListResultView view, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
        List<Record> records = view.getObject();
        List<String> headers = getHeaders(records.get(0));
        entityStream.write((String.join(",", headers) + "\r\n").getBytes("utf-8"));
        for (Record record : records) {
            writeRow(entityStream, headers, record);
        }
    }

    private void writeRow(OutputStream entityStream, List<String> headers, Record node) throws IOException {
        Map<String, Object> entry = JsonObjectMapper.convert(node.getContent(), new TypeReference<Map<String, Object>>() {
        });
        entry.put("hash", node.getHash());
        String row = headers.stream().map(e -> escape(entry.get(e).toString())).collect(Collectors.joining(",", "", "\r\n"));
        entityStream.write(row.getBytes("utf-8"));
    }

    private String escape(String data) {
        return StringEscapeUtils.escapeCsv(data);
    }

    private List<String> getHeaders(Record record) {
        List<String> headers = new ArrayList<>();
        headers.add("hash");
        Iterators.addAll(headers, record.getContent().fieldNames());
        return headers;
    }
}
