//package uk.gov.admin;
//
//import org.junit.Test;
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.StringReader;
//import java.net.URISyntaxException;
//import java.util.List;
//
//import static org.hamcrest.CoreMatchers.equalTo;
//import static org.hamcrest.MatcherAssert.assertThat;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.when;
//
//public class ToJSONLConverterTest {
//
//    @Test
//    public void should_be_able_to_process_empty_tsv() throws URISyntaxException, IOException {
//        String sampleTsv = "";
//
//        ToJSONLConverter toJSONLConverter = ToJSONLConverter.converterFor("tsv");
//
//        DataReader reader = mock(DataReader.class);
//        when(reader.reader()).thenReturn(new BufferedReader(new StringReader(sampleTsv)));
//
//        List<String> converted = toJSONLConverter.convert(reader);
//        assertThat(converted.size(), equalTo(0));
//    }
//
//    @Test
//    public void should_be_able_to_convert_simple_csv_to_jsonl() {
//        String sampleTsv = "firstName,lastName,town\nBob,Jones,Holborn";
//        String expectedJsonl = "{\"firstName\": \"Bob\", \"lastName\": \"Jones\", \"town\": \"Holborn\"}";
//
//        DataReader reader = mock(DataReader.class);
//        when(reader.reader()).thenReturn(new BufferedReader(new StringReader(sampleTsv)));
//
//        ToJSONLConverter toJSONLConverter = ToJSONLConverter.converterFor("csv");
//        List<String> converted = toJSONLConverter.convert(reader);
//        assertThat(converted.get(0), equalTo(expectedJsonl));
//    }
//
//    @Test
//    public void should_be_able_to_convert_simple_tsv_to_jsonl() {
//        String sampleTsv = "firstName\tlastName\ttown\nBob\tJones\tHolborn";
//        String expectedJsonl = "{\"firstName\": \"Bob\", \"lastName\": \"Jones\", \"town\": \"Holborn\"}";
//
//        DataReader reader = mock(DataReader.class);
//        when(reader.reader()).thenReturn(new BufferedReader(new StringReader(sampleTsv)));
//
//        ToJSONLConverter toJSONLConverter = ToJSONLConverter.converterFor("tsv");
//        List<String> converted = toJSONLConverter.convert(reader);
//        assertThat(converted.get(0), equalTo(expectedJsonl));
//    }
//
//    @Test
//    public void should_be_able_to_convert_tsv_with_mismatched_headers_to_jsonl() {
//        String sampleTsv = "firstName\tlastName\nBob\tJones\tHolborn";
//        String expectedJsonl = "{\"firstName\": \"Bob\", \"lastName\": \"Jones\"}";
//
//        DataReader reader = mock(DataReader.class);
//        when(reader.reader()).thenReturn(new BufferedReader(new StringReader(sampleTsv)));
//
//        ToJSONLConverter toJSONLConverter = ToJSONLConverter.converterFor("tsv");
//        List<String> converted = toJSONLConverter.convert(reader);
//        assertThat(converted.get(0), equalTo(expectedJsonl));
//    }
//
//    @Test
//    public void should_be_able_to_convert_tsv_with_mismatched_fields_to_jsonl() {
//        String sampleTsv = "firstName\tlastName\ttown\nBob\tJones\t";
//        String expectedJsonl = "{\"firstName\": \"Bob\", \"lastName\": \"Jones\", \"town\": \"\"}";
//
//        DataReader reader = mock(DataReader.class);
//        when(reader.reader()).thenReturn(new BufferedReader(new StringReader(sampleTsv)));
//
//        ToJSONLConverter toJSONLConverter = ToJSONLConverter.converterFor("tsv");
//        List<String> converted = toJSONLConverter.convert(reader);
//        assertThat(converted.get(0), equalTo(expectedJsonl));
//    }
//
//    @Test
//    public void should_be_able_to_convert_tsv_with_mismatched_data_to_jsonl() {
//        String sampleTsv = "firstName\tlastName\ttown\nBob\t\tHolborn";
//        String expectedJsonl = "{\"firstName\": \"Bob\", \"lastName\": \"\", \"town\": \"Holborn\"}";
//
//        DataReader reader = mock(DataReader.class);
//        when(reader.reader()).thenReturn(new BufferedReader(new StringReader(sampleTsv)));
//
//        ToJSONLConverter toJSONLConverter = ToJSONLConverter.converterFor("tsv");
//        List<String> converted = toJSONLConverter.convert(reader);
//        assertThat(converted.get(0), equalTo(expectedJsonl));
//    }
//}
