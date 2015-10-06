package uk.gov.register.presentation.config;

import com.google.common.collect.ImmutableSet;
import org.hamcrest.collection.IsIterableContainingInOrder;
import org.junit.Test;

import static org.junit.Assert.assertThat;

public class RegisterTest {
    @Test
    public void getFields_returnsFieldsInSortedOrder() throws Exception {
        Register register = new Register("address", ImmutableSet.of("address", "street", "postcode", "area", "property"), "", new PublicBody("",""), "");

        Iterable<String> fields = register.getFields();

        assertThat(fields, IsIterableContainingInOrder.contains("address", "area", "postcode", "property", "street"));
    }

    @Test
    public void getNonPrimaryFields_returnsFieldsOtherThanPrimaryInSortedOrder() throws Exception {
        Register register = new Register("company", ImmutableSet.of("address", "company", "secretary", "company-status", "company-accounts-category"), "", new PublicBody("",""), "");

        Iterable<String> fields = register.getNonPrimaryFields();

        assertThat(fields, IsIterableContainingInOrder.contains("address", "company-accounts-category", "company-status", "secretary"));
    }
}
