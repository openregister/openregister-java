package uk.gov.functional;

import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import uk.gov.MintApplication;
import uk.gov.functional.db.TestDBItem;

import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.functional.TestDBSupport.*;

public class DataMigrationTest {

    {
        testEntriesDAO.dropTable();
        testItemDAO.dropTable();
        testEntryDAO.dropTable();

        testEntriesDAO.createTable();

        String oldEntry1 = "{\"hash\":\"hash1\",\"entry\":{\"register\":\"register1\",\"text\":\"some text\"}}";
        String oldEntry2 = "{\"hash\":\"hash2\",\"entry\":{\"register\":\"register2\",\"text\":\"some other text\"}}";

        testEntriesDAO.load(oldEntry1.getBytes());
        testEntriesDAO.load(oldEntry2.getBytes());
        testEntriesDAO.load(oldEntry1.getBytes());
    }

    @Rule
    public TestRule ruleChain = RuleChain.
            outerRule(
                    new DropwizardAppRule<>(MintApplication.class,
                            ResourceHelpers.resourceFilePath("test-config.yaml"),
                            ConfigOverride.config("database.url", postgresConnectionString))
            );

    @Test
    public void dataIsMigratedFromOldTableToNewTables() throws InterruptedException {
        Thread.sleep(1000);
        List<TestDBItem> items = testItemDAO.getItems();
        assertThat(items.size(), equalTo(2));
        byte[] item1Bytes = "{\"register\":\"register1\",\"text\":\"some text\"}".getBytes();
        assertThat(items.get(0).contents, equalTo(item1Bytes));
        assertThat(items.get(0).sha256hex, equalTo(DigestUtils.sha256Hex(item1Bytes)));

        byte[] item2Bytes = "{\"register\":\"register2\",\"text\":\"some other text\"}".getBytes();
        assertThat(items.get(1).contents, equalTo(item2Bytes));
        assertThat(items.get(1).sha256hex, equalTo(DigestUtils.sha256Hex(item2Bytes)));

        List<String> allHex = testEntryDAO.getAllHex();
        assertThat(allHex.size(), equalTo(3));
        assertThat(allHex.get(0), equalTo(DigestUtils.sha256Hex(item1Bytes)));
        assertThat(allHex.get(1), equalTo(DigestUtils.sha256Hex(item2Bytes)));
        assertThat(allHex.get(2), equalTo(DigestUtils.sha256Hex(item1Bytes)));
    }
}
