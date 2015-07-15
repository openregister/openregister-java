package uk.gov.register.presentation.entity;

import uk.gov.register.presentation.mapper.JsonObjectMapper;
import uk.gov.register.presentation.view.AbstractView;

import java.util.*;
import java.util.stream.Collectors;

class CsvEntity implements Entity {

    private final String separator;

    public CsvEntity(String separator) {
        this.separator = separator;
    }

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


        return String.join(separator, headerElements) + "\n" + data;
    }

    private List<String> headerElements(Map map){
        List<String> headerElements = new ArrayList<>();
        headerElements.add("hash");
        headerElements.addAll(((Map) map.get("entry")).keySet());
        return headerElements;
    }

    private String getCsvData(List<String> headerElements, Map map) {
        Map entry = (Map) map.get("entry");
        entry.put("hash", map.get("hash"));

        return headerElements.stream().map(e -> (String) entry.get(e)).collect(Collectors.joining(separator));
    }

    @Override
    public String contentType() {
        if(separator.equals(",")){
            return "text/csv";
        }else{
            return "text/tsv";
        }
    }
}

