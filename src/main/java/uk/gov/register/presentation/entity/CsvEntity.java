package uk.gov.register.presentation.entity;

import org.apache.commons.lang3.StringEscapeUtils;
import uk.gov.register.presentation.mapper.JsonObjectMapper;
import uk.gov.register.presentation.view.AbstractView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class CsvEntity implements Entity {

    @SuppressWarnings("unchecked")
    @Override
    public Object convert(AbstractView view) {
        //Todo: this must be all the register fields
        final List<String> headerElements;

        String data;
        if (view.get() instanceof List) {
            List<Map> list = JsonObjectMapper.convert(view.get(), List.class);

            headerElements = headerElements(list.get(0));

            data = list.stream().map(e -> getCsvData(headerElements, e)).collect(Collectors.joining("\n"));
        } else {
            Map map = JsonObjectMapper.convert(view.get(), Map.class);

            headerElements = headerElements(map);

            data = getCsvData(headerElements, map);
        }


        return String.join(entrySeparator(), headerElements) + "\n" + data;
    }

    @SuppressWarnings("unchecked")
    private List<String> headerElements(Map map) {
        List<String> headerElements = new ArrayList<>();
        headerElements.add("hash");
        headerElements.addAll(((Map) map.get("entry")).keySet());
        return headerElements;
    }

    @SuppressWarnings("unchecked")
    private String getCsvData(List<String> headerElements, Map map) {
        Map<String, Object> entry = (Map) map.get("entry");
        entry.put("hash", map.get("hash"));

        return headerElements.stream().map(e -> escape((String) entry.get(e))).collect(Collectors.joining(entrySeparator()));
    }

    @Override
    public String contentType() {
        return "text/csv; charset=utf-8";
    }

    protected String escape(String data) {
        return StringEscapeUtils.escapeCsv(data);
    }

    protected String entrySeparator(){
        return ",";
    }
}

