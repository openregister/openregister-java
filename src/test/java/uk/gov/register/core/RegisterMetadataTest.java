package uk.gov.register.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Resources;
import io.dropwizard.jackson.Jackson;
import org.hamcrest.collection.IsIterableContainingInOrder;
import org.junit.Test;

import static java.util.Collections.emptyList;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;

public class RegisterMetadataTest {
    private static final ObjectMapper YAML_MAPPER = Jackson.newObjectMapper(new YAMLFactory());

    @Test
    public void getFields_returnsFieldsInConfiguredOrder() throws Exception {
        RegisterMetadata registerMetadata = new RegisterMetadata(new RegisterName("address"), ImmutableList.of("address", "street", "postcode", "area", "property"), "", "", "", "alpha");

        Iterable<String> fields = registerMetadata.getFields();

        assertThat(fields, IsIterableContainingInOrder.contains("address", "street", "postcode", "area", "property"));
    }

    @Test
    public void getNonPrimaryFields_returnsFieldsOtherThanPrimaryInConfiguredOrder() throws Exception {
        RegisterMetadata registerMetadata = new RegisterMetadata(new RegisterName("company"), ImmutableList.of("address", "company", "secretary", "company-status", "company-accounts-category"), "", "", "", "alpha");

        Iterable<String> fields = registerMetadata.getNonPrimaryFields();

        assertThat(fields, IsIterableContainingInOrder.contains("address", "secretary", "company-status", "company-accounts-category"));
    }

    @Test
    public void shouldDeserializeUndeclaredFields() throws Exception {
        RegisterMetadata registerMetadata = YAML_MAPPER.readValue(
                Resources.getResource("fixtures/local-authority-eng-register-record.yaml"),
                RegisterMetadata.class);

        assertThat(registerMetadata.getCopyright(), is(nullValue()));
        assertThat(registerMetadata.getPhase(), is("beta"));
        assertThat(registerMetadata.getOtherProperties().get("entry-timestamp"), is(new TextNode("2016-10-21T15:20:42Z")));
    }

    @Test
    public void shouldSerializeUndeclaredFields() throws Exception {
        RegisterMetadata registerMetadata = new RegisterMetadata(new RegisterName("test-register"), emptyList(), null, null, "a test register", "discovery");
        registerMetadata.setOtherProperty("foo", new TextNode("bar"));

        String output = YAML_MAPPER.writeValueAsString(registerMetadata);

        assertThat(output, containsString("register: \"test-register\""));
        assertThat(output, containsString("foo: \"bar\""));
    }

    @Test
    public void shouldRoundTripUndeclaredFields() throws Exception {
        RegisterMetadata registerMetadata = new RegisterMetadata(new RegisterName("test-register"), emptyList(), null, null, "a test register", "discovery");
        registerMetadata.setOtherProperty("foo", new TextNode("bar"));

        String output = YAML_MAPPER.writeValueAsString(registerMetadata);
        RegisterMetadata roundTripped = YAML_MAPPER.readValue(output, RegisterMetadata.class);

        assertThat(roundTripped.getRegisterName(), is(new RegisterName("test-register")));
        assertThat(roundTripped.getOtherProperties().get("foo"), is(new TextNode("bar")));
    }

    @Test
    public void shouldNotSerializeNullFields() throws Exception {
        RegisterMetadata registerMetadata = new RegisterMetadata(new RegisterName("test-register"), emptyList(), null, null, "a test register", "discovery");

        String output = YAML_MAPPER.writeValueAsString(registerMetadata);

        assertThat(output, containsString("register: \"test-register\""));
        assertThat(output, not(containsString("copyright")));
        assertThat(output, not(containsString("null")));
    }
}
