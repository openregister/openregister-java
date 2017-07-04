package uk.gov.register.functional.helpers;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class RecordJsonComparisonHelper {
	private static ObjectMapper objectMapper = new ObjectMapper();
	
	public static void assertJsonEqual(String json1, String json2) throws IOException {
		TreeMap<String, Object> jsonNodes1 = new TreeMap<>(objectMapper.readValue(json1, Map.class));
		TreeMap<String, Object> jsonNodes2 = new TreeMap<>(objectMapper.readValue(json2, Map.class));
		
		assertThat(jsonNodes1.size(), equalTo(jsonNodes2.size()));
		
		jsonNodes1.entrySet().stream().forEach(pair -> {
			assertThat(jsonNodes1.containsKey(pair.getKey()) && jsonNodes2.containsKey(pair.getKey()), equalTo(true));
			
			Map<String, Object> jsonNode1 = (Map<String, Object>) jsonNodes1.get(pair.getKey());
			Map<String, Object> jsonNode2 = (Map<String, Object>) jsonNodes2.get(pair.getKey());

			assertThat(jsonNode1.get("index-entry-number").equals(jsonNode2.get("index-entry-number")), equalTo(true));
			assertThat(jsonNode1.get("entry-number").equals(jsonNode2.get("entry-number")), equalTo(true));
			assertThat(jsonNode1.get("key").equals(jsonNode2.get("key")), equalTo(true));
			
			List<Map<String, String>> jsonNode1Items = (List<Map<String, String>>) jsonNode1.get("item");
			List<Map<String, String>> jsonNode2Items = (List<Map<String, String>>) jsonNode2.get("item");
			
			assertThat(jsonNode1Items.size(), equalTo(jsonNode2Items.size()));
			assertThat(jsonNode1Items.containsAll(jsonNode2Items), equalTo(true));
		});
	}
}
