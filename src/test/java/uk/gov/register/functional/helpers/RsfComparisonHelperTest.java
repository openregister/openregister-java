package uk.gov.register.functional.helpers;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class RsfComparisonHelperTest {
	@Before
	public void setup() {
		System.setProperty("multi-item-entries-enabled", "true");
	}
	
	@After
	public void teardown() {
		System.clearProperty("multi-item-entries-enabled");
	}
	
	@Test
	public void assertRsfEqual_shouldProcessRsfWithoutException_whenRsfAreEqual() {
		String rsf1 = 
			"assert-root-hash\tsha-256:xyz\n" +
			"add-item\t{\"name\":\"A\",\"type\":\"MD\"}\n" +
			"add-item\t{\"name\":\"B\"\"type\":\"UA\"}\n" +
			"add-item\t{\"name\":\"C\"\"type\":\"CTY\"}\n" +
			"append-entry\tuser\tA\t2016-04-05T13:23:05Z\tsha-256:6f1d2336f50812a0b412754217380518f84c72c8e725abc9a6071da0d44b7fab\n" +
			"append-entry\tuser\tB\t2016-04-05T13:23:05Z\tsha-256:a58992124b16900f0a831da520f4eec963e93a16ebba5dd94cd0b8739e401969\n" +
			"append-entry\tuser\tC\t2016-04-05T13:23:05Z\tsha-256:a8cd23fabc904199315f369a289a7bbabe1a1ec88a9bc28af0f34f97e0034bac";
		
		RsfComparisonHelper.assertRsfEqual(rsf1, new String(rsf1));
	}
	
	@Test(expected = AssertionError.class)
	public void assertRsfEqual_shouldProcessRsfWithException_whenRsfAreNotEqual() {
		String rsf1 = "assert-root-hash\tsha-256:xyz";
		String rsf2 = "assert-root-hash\tsha-256:abc";
		
		RsfComparisonHelper.assertRsfEqual(rsf1, rsf2);
	}
	
	@Test
	public void assertRsfEqual_shouldProcessRsfWithoutException_whenItemsAreInDifferentOrder() {
		String rsf1 =
				"assert-root-hash\tsha-256:xyz\n" +
				"add-item\t{\"name\":\"A\",\"type\":\"MD\"}\n" +
				"add-item\t{\"name\":\"B\"\"type\":\"UA\"}\n" +
				"add-item\t{\"name\":\"C\"\"type\":\"CTY\"}\n" +
				"append-entry\tuser\tA\t2016-04-05T13:23:05Z\tsha-256:6f1d2336f50812a0b412754217380518f84c72c8e725abc9a6071da0d44b7fab\n" +
				"append-entry\tuser\tB\t2016-04-05T13:23:05Z\tsha-256:a58992124b16900f0a831da520f4eec963e93a16ebba5dd94cd0b8739e401969\n" +
				"append-entry\tuser\tC\t2016-04-05T13:23:05Z\tsha-256:a8cd23fabc904199315f369a289a7bbabe1a1ec88a9bc28af0f34f97e0034bac";

		String rsf2 =
				"assert-root-hash\tsha-256:xyz\n" +
				"add-item\t{\"name\":\"C\"\"type\":\"CTY\"}\n" +
				"add-item\t{\"name\":\"A\",\"type\":\"MD\"}\n" +
				"add-item\t{\"name\":\"B\"\"type\":\"UA\"}\n" +
				"append-entry\tuser\tA\t2016-04-05T13:23:05Z\tsha-256:6f1d2336f50812a0b412754217380518f84c72c8e725abc9a6071da0d44b7fab\n" +
				"append-entry\tuser\tB\t2016-04-05T13:23:05Z\tsha-256:a58992124b16900f0a831da520f4eec963e93a16ebba5dd94cd0b8739e401969\n" +
				"append-entry\tuser\tC\t2016-04-05T13:23:05Z\tsha-256:a8cd23fabc904199315f369a289a7bbabe1a1ec88a9bc28af0f34f97e0034bac";

		RsfComparisonHelper.assertRsfEqual(rsf1, rsf2);
	}
	
	@Test
	public void assertRsfEqual_shouldProcessRsfWithoutException_whenEntryContainsMultipleItemHashesInDifferentOrder() {
		String rsf1 =
				"assert-root-hash\tsha-256:xyz\n" +
				"add-item\t{\"name\":\"A\",\"type\":\"MD\"}\n" +
				"add-item\t{\"name\":\"B\"\"type\":\"UA\"}\n" +
				"add-item\t{\"name\":\"C\"\"type\":\"CTY\"}\n" +
				"append-entry\tuser\tY\t2016-04-05T13:23:05Z\tsha-256:6f1d2336f50812a0b412754217380518f84c72c8e725abc9a6071da0d44b7fab\n" +
				"append-entry\tuser\tZ\t2016-04-05T13:23:05Z\tsha-256:a58992124b16900f0a831da520f4eec963e93a16ebba5dd94cd0b8739e401969\n" +
				"append-entry\tuser\tZ\t2016-04-05T13:23:05Z\tsha-256:a58992124b16900f0a831da520f4eec963e93a16ebba5dd94cd0b8739e401969;sha-256:a8cd23fabc904199315f369a289a7bbabe1a1ec88a9bc28af0f34f97e0034bac";

		String rsf2 =
				"assert-root-hash\tsha-256:xyz\n" +
				"add-item\t{\"name\":\"A\",\"type\":\"MD\"}\n" +
				"add-item\t{\"name\":\"B\"\"type\":\"UA\"}\n" +
				"add-item\t{\"name\":\"C\"\"type\":\"CTY\"}\n" +
				"append-entry\tuser\tY\t2016-04-05T13:23:05Z\tsha-256:6f1d2336f50812a0b412754217380518f84c72c8e725abc9a6071da0d44b7fab\n" +
				"append-entry\tuser\tZ\t2016-04-05T13:23:05Z\tsha-256:a58992124b16900f0a831da520f4eec963e93a16ebba5dd94cd0b8739e401969\n" +
				"append-entry\tuser\tZ\t2016-04-05T13:23:05Z\tsha-256:a8cd23fabc904199315f369a289a7bbabe1a1ec88a9bc28af0f34f97e0034bac;sha-256:a58992124b16900f0a831da520f4eec963e93a16ebba5dd94cd0b8739e401969";

		RsfComparisonHelper.assertRsfEqual(rsf1, rsf2);
	}
	
	@Test
	public void assertRsfEqual_shouldProcessRsfWithoutException_whenEntryContainsMultipleItemsAndItemHashesInDifferentOrder() {
		String rsf1 =
				"assert-root-hash\tsha-256:xyz\n" +
				"add-item\t{\"name\":\"A\",\"type\":\"MD\"}\n" +
				"add-item\t{\"name\":\"B\"\"type\":\"UA\"}\n" +
				"add-item\t{\"name\":\"C\"\"type\":\"CTY\"}\n" +
				"append-entry\tuser\tY\t2016-04-05T13:23:05Z\tsha-256:6f1d2336f50812a0b412754217380518f84c72c8e725abc9a6071da0d44b7fab\n" +
				"append-entry\tuser\tZ\t2016-04-05T13:23:05Z\tsha-256:a58992124b16900f0a831da520f4eec963e93a16ebba5dd94cd0b8739e401969\n" +
				"append-entry\tuser\tZ\t2016-04-05T13:23:05Z\tsha-256:a58992124b16900f0a831da520f4eec963e93a16ebba5dd94cd0b8739e401969;sha-256:a8cd23fabc904199315f369a289a7bbabe1a1ec88a9bc28af0f34f97e0034bac";

		String rsf2 =
				"assert-root-hash\tsha-256:xyz\n" +
				"add-item\t{\"name\":\"C\"\"type\":\"CTY\"}\n" +
				"add-item\t{\"name\":\"A\",\"type\":\"MD\"}\n" +
				"add-item\t{\"name\":\"B\"\"type\":\"UA\"}\n" +
				"append-entry\tuser\tY\t2016-04-05T13:23:05Z\tsha-256:6f1d2336f50812a0b412754217380518f84c72c8e725abc9a6071da0d44b7fab\n" +
				"append-entry\tuser\tZ\t2016-04-05T13:23:05Z\tsha-256:a58992124b16900f0a831da520f4eec963e93a16ebba5dd94cd0b8739e401969\n" +
				"append-entry\tuser\tZ\t2016-04-05T13:23:05Z\tsha-256:a8cd23fabc904199315f369a289a7bbabe1a1ec88a9bc28af0f34f97e0034bac;sha-256:a58992124b16900f0a831da520f4eec963e93a16ebba5dd94cd0b8739e401969";

		RsfComparisonHelper.assertRsfEqual(rsf1, rsf2);
	}
}
