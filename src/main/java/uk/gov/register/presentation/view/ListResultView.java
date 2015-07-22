package uk.gov.register.presentation.view;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.dropwizard.views.View;
import uk.gov.register.presentation.Entry;
import uk.gov.register.presentation.mapper.JsonObjectMapper;
import uk.gov.register.presentation.representations.ListResultJsonSerializer;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@JsonSerialize(using = ListResultJsonSerializer.class)
public class ListResultView extends View {
    private final List<Entry> entries;

    public ListResultView(String templateName, List<Entry> entries) {
        super(templateName);
        this.entries = entries;
    }

    public List<Entry> getObject() {
        return entries;
    }

    List<Set> getEntries() {
        return entries.stream().map(e -> {
            Map<String, Object> entry = JsonObjectMapper.convert(e.getContent(), new TypeReference<Map<String, Object>>(){});
            entry.put("hash", e.getHash());
            return entry.entrySet();
        }).collect(Collectors.toList());
    }
}
