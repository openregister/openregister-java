package uk.gov.register.functional.helpers;

import org.junit.Test;

import java.io.IOException;

public class RecordJsonComparisonHelperTest {
	@Test
	public void assertJsonEqual_shouldProcessJsonWithoutException_whenObjectsAreEqual() throws IOException {
		String json1 = "{\"UA\": {\"entry-number\":\"3\", \"index-entry-number\":\"3\", \"key\": \"A\", \"item\": []}}";
		String json2 = "{\"UA\": {\"entry-number\":\"3\", \"index-entry-number\":\"3\", \"key\": \"A\", \"item\": []}}";
		
		RecordJsonComparisonHelper.assertJsonEqual(json1, json2);
	}

	@Test(expected = AssertionError.class)
	public void assertJsonEqual_shouldProcessJsonWithException_whenObjectsAreNotEqual() throws IOException {
		String json1 = "{\"UA\": {\"entry-number\":\"3\", \"index-entry-number\":\"3\", \"key\": \"A\", \"item\": []}}";
		String json2 = "{\"CTY\": {\"entry-number\":\"4\", \"index-entry-number\":\"4\", \"key\": \"B\", \"item\": []}}";

		RecordJsonComparisonHelper.assertJsonEqual(json1, json2);
	}
	
	@Test
	public void assertJsonEqual_shouldProcessJsonWithoutException_whenObjectsInDifferentOrder() throws IOException {
		String json1 = "{\"UA\": {\"entry-number\":\"3\", \"index-entry-number\":\"3\", \"key\": \"A\", \"item\": []}, \"CTY\": {\"entry-number\":\"4\", \"index-entry-number\":\"4\", \"key\": \"B\", \"item\": []}}";
		String json2 = "{\"CTY\": {\"entry-number\":\"4\", \"index-entry-number\":\"4\", \"key\": \"B\", \"item\": []}, \"UA\": {\"entry-number\":\"3\", \"index-entry-number\":\"3\", \"key\": \"A\", \"item\": []}}";

		RecordJsonComparisonHelper.assertJsonEqual(json1, json2);
	}
	
	@Test
	public void assertJsonEqual_shouldProcessJsonWithoutException_whenObjectsContainArrayPropertyInSameOrder() throws IOException {
		String json1 = "{\"UA\": {\"entry-number\":\"3\", \"index-entry-number\":\"3\", \"key\": \"A\", \"item\": [{\"name\":\"A\", \"type\":\"UA\"}, {\"name\":\"B\", \"type\":\"UA\"}]}}";
		String json2 = "{\"UA\": {\"entry-number\":\"3\", \"index-entry-number\":\"3\", \"key\": \"A\", \"item\": [{\"name\":\"A\", \"type\":\"UA\"}, {\"name\":\"B\", \"type\":\"UA\"}]}}";

		RecordJsonComparisonHelper.assertJsonEqual(json1, json2);
	}
	
	@Test
	public void assertJsonEqual_shouldProcessJsonWithoutException_whenObjectsContainArrayPropertyInDifferentOrder() throws IOException {
		String json1 = "{\"UA\": {\"entry-number\":\"3\", \"index-entry-number\":\"3\", \"key\": \"A\", \"item\": [{\"name\":\"A\", \"type\":\"UA\"}, {\"name\":\"B\", \"type\":\"UA\"}]}}";
		String json2 = "{\"UA\": {\"entry-number\":\"3\", \"index-entry-number\":\"3\", \"key\": \"A\", \"item\": [{\"name\":\"B\", \"type\":\"UA\"}, {\"name\":\"A\", \"type\":\"UA\"}]}}";

		RecordJsonComparisonHelper.assertJsonEqual(json1, json2);
	}
	
	@Test
	public void assertJsonEqual_shouldProcessJsonWithoutException_whenObjectsAndArrayPropertiesInDifferentOrder() throws IOException {
		String json1 = "{\"UA\": {\"entry-number\":\"3\", \"index-entry-number\":\"3\", \"key\":\"A\", \"item\":[{\"name\":\"A\", \"type\":\"UA\"}, {\"name\":\"B\", \"type\":\"UA\"}]}, \"CTY\": {\"entry-number\":\"4\", \"index-entry-number\":\"4\", \"key\":\"B\", \"item\":[{\"name\":\"Y\", \"type\":\"CTY\"}, {\"name\":\"Z\", \"type\":\"CTY\"}]}}";
		String json2 = "{\"CTY\": {\"entry-number\":\"4\", \"index-entry-number\":\"4\", \"key\":\"B\", \"item\":[{\"name\":\"Z\", \"type\":\"CTY\"}, {\"name\":\"Y\", \"type\":\"CTY\"}]}, \"UA\": {\"entry-number\":\"3\", \"index-entry-number\":\"3\", \"key\":\"A\", \"item\":[{\"name\":\"B\", \"type\":\"UA\"}, {\"name\":\"A\", \"type\":\"UA\"}]}}";

		RecordJsonComparisonHelper.assertJsonEqual(json1, json2);
	}
}
