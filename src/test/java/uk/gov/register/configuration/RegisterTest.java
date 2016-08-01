package uk.gov.register.configuration;

import com.google.common.collect.ImmutableList;
import org.hamcrest.collection.IsIterableContainingInOrder;
import org.junit.Test;
import uk.gov.register.core.Register;

import static org.junit.Assert.assertThat;

public class RegisterTest {
    @Test
    public void getFields_returnsFieldsInConfiguredOrder() throws Exception {
        Register register = new Register("address", ImmutableList.of("address", "street", "postcode", "area", "property"), "", "", "", "alpha");

        Iterable<String> fields = register.getFields();

        assertThat(fields, IsIterableContainingInOrder.contains("address", "street", "postcode", "area", "property"));
    }

    @Test
    public void getNonPrimaryFields_returnsFieldsOtherThanPrimaryInConfiguredOrder() throws Exception {
        Register register = new Register("company", ImmutableList.of("address", "company", "secretary", "company-status", "company-accounts-category"), "", "", "", "alpha");

        Iterable<String> fields = register.getNonPrimaryFields();

        assertThat(fields, IsIterableContainingInOrder.contains("address","secretary", "company-status", "company-accounts-category"));
    }
}
