package uk.gov.register.presentation.view;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.dropwizard.views.View;
import uk.gov.register.presentation.Record;
import uk.gov.register.presentation.mapper.JsonObjectMapper;
import uk.gov.register.presentation.representations.ListResultJsonSerializer;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@JsonSerialize(using = ListResultJsonSerializer.class)
public class ListResultView extends View {
    private final List<Record> records;

    public ListResultView(String templateName, List<Record> records) {
        super(templateName);
        this.records = records;
    }

    public List<Record> getObject() {
        return records;
    }

    List<Set> getRecords() {
        return records.stream().map(e -> {
            Map<String, Object> entry = JsonObjectMapper.convert(e.getContent(), new TypeReference<Map<String, Object>>(){});
            entry.put("hash", e.getHash());
            return entry.entrySet();
        }).collect(Collectors.toList());
    }
}
