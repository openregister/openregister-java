package uk.gov.admin;

public class ToJSONLConverterTest {

//    @Test
//    public void should_be_able_to_process_empty_tsv() {
//        final List<String> sampleTsv = Arrays.asList("");
//
//        final ToJSONLConverter toJSONLConverter = ToJSONLConverter.converterFor(ToJSONLConverter.ConvertableType.tsv);
//        final List<String> converted = toJSONLConverter.convert(sampleTsv);
//        Assert.assertEquals(0, converted.size());
//    }
//
//    @Test
//    public void should_be_able_to_convert_simple_tsv_to_jsonl() {
//        final List<String> sampleTsv = Arrays.asList("firstName\tlastName\ttown", "Bob\tJones\tHolborn");
//        final String expectedJsonl = "{\"firstName\": \"Bob\", \"lastName\": \"Jones\", \"town\": \"Holborn\"}";
//
//        final ToJSONLConverter toJSONLConverter = ToJSONLConverter.converterFor(ToJSONLConverter.ConvertableType.tsv);
//        final List<String> converted = toJSONLConverter.convert(sampleTsv);
//        Assert.assertEquals(expectedJsonl, converted.get(0));
//    }
//
//    @Test
//    public void should_be_able_to_convert_tsv_with_mismatched_headers_to_jsonl() {
//        final List<String> sampleTsv = Arrays.asList("firstName\tlastName", "Bob\tJones\tHolborn");
//        final String expectedJsonl = "{\"firstName\": \"Bob\", \"lastName\": \"Jones\"}";
//
//        final ToJSONLConverter toJSONLConverter = ToJSONLConverter.converterFor(ToJSONLConverter.ConvertableType.tsv);
//        final List<String> converted = toJSONLConverter.convert(sampleTsv);
//        Assert.assertEquals(expectedJsonl, converted.get(0));
//    }
//
//    @Test
//    public void should_be_able_to_convert_tsv_with_mismatched_fields_to_jsonl() {
//        final List<String> sampleTsv = Arrays.asList("firstName\tlastName\ttown", "Bob\tJones\t");
//        final String expectedJsonl = "{\"firstName\": \"Bob\", \"lastName\": \"Jones\", \"town\": \"\"}";
//
//        final ToJSONLConverter toJSONLConverter = ToJSONLConverter.converterFor(ToJSONLConverter.ConvertableType.tsv);
//        final List<String> converted = toJSONLConverter.convert(sampleTsv);
//        Assert.assertEquals(expectedJsonl, converted.get(0));
//    }
//
//    @Test
//    public void should_be_able_to_convert_tsv_with_mismatched_data_to_jsonl() {
//        final List<String> sampleTsv = Arrays.asList("firstName\tlastName\ttown", "Bob\t\tHolborn");
//        final String expectedJsonl = "{\"firstName\": \"Bob\", \"lastName\": \"\", \"town\": \"Holborn\"}";
//
//        final ToJSONLConverter toJSONLConverter = ToJSONLConverter.converterFor(ToJSONLConverter.ConvertableType.tsv);
//        final List<String> converted = toJSONLConverter.convert(sampleTsv);
//        Assert.assertEquals(expectedJsonl, converted.get(0));
//    }
}
