package uk.gov.register.core;

import com.google.common.collect.ImmutableList;
import org.hamcrest.collection.IsIterableContainingInOrder;
import org.junit.Test;

import static org.junit.Assert.assertThat;

public class RegisterMetadataTest {
    @Test
    public void getFields_returnsFieldsInConfiguredOrder() throws Exception {
        RegisterMetadata registerMetadata = new RegisterMetadata("address", ImmutableList.of("address", "street", "postcode", "area", "property"), "", "", "", "alpha");

        Iterable<String> fields = registerMetadata.getFields();

        assertThat(fields, IsIterableContainingInOrder.contains("address", "street", "postcode", "area", "property"));
    }

    @Test
    public void getNonPrimaryFields_returnsFieldsOtherThanPrimaryInConfiguredOrder() throws Exception {
        RegisterMetadata registerMetadata = new RegisterMetadata("company", ImmutableList.of("address", "company", "secretary", "company-status", "company-accounts-category"), "", "", "", "alpha");

        Iterable<String> fields = registerMetadata.getNonPrimaryFields();

        assertThat(fields, IsIterableContainingInOrder.contains("address","secretary", "company-status", "company-accounts-category"));
    }
}
