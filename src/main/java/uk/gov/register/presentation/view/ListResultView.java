package uk.gov.register.presentation.view;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import uk.gov.register.presentation.Entry;
import uk.gov.register.presentation.mapper.JsonObjectMapper;
import uk.gov.register.presentation.representations.ListResultJsonSerializer;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@JsonSerialize(using = ListResultJsonSerializer.class)
public class ListResultView extends AbstractView<List<Entry>> {
    private final List<Entry> entries;

    public ListResultView(String templateName, List<Entry> entries) {
        super(templateName);
        this.entries = entries;
    }

    @Override
    public List<Entry> get() {
        return entries;
    }

    List<Set> getEntries() {
        return entries.stream().map(n -> JsonObjectMapper.convert(n, Map.class)).map(m -> {
            Map entry = (Map) m.get("entry");
            //noinspection unchecked
            entry.put("hash", m.get("hash"));
            return entry.entrySet();
        }).collect(Collectors.toList());
    }
}
