package uk.gov.register.service;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import uk.gov.register.core.Register;
import uk.gov.register.core.RegisterContext;
import uk.gov.register.serialization.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.function.Consumer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class RegisterSerialisationFormatServiceTest {
    @Mock
    private RegisterContext registerContext;

    @Mock
    private Register register;

    @Mock
    private RSFCreator rsfCreator;

    @Mock
    private RSFExecutor rsfExecutor;

    private RSFFormatter rsfFormatter;
    private RegisterSerialisationFormatService sutService;

    @Before
    public void setUp() throws Exception {
        doAnswer(new Answer<Void>() {
            public Void answer(InvocationOnMock invocation) {
                Consumer<Register> callback = (Consumer<Register>) invocation.getArguments()[0];
                callback.accept(register);
                return null;
            }
        }).when(registerContext).transactionalRegisterOperation(any(Consumer.class));

        sutService = new RegisterSerialisationFormatService(registerContext, rsfExecutor, rsfCreator);
        rsfFormatter = new RSFFormatter();
    }

    @Test
    public void process_passesCommandsToExecutorAndReturnsItsResult() {
        RegisterCommand command1 = new RegisterCommand("assert-root-hash", Collections.singletonList("sha-256:e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"));
        RegisterCommand command2 = new RegisterCommand("add-item", Collections.singletonList("{\"field-1\":\"entry1-field-1-value\",\"field-2\":\"entry1-field-2-value\"}"));
        RegisterCommand command3 = new RegisterCommand("append-entry", Arrays.asList("user", "entry1-field-1-value", "2016-07-24T16:55:00Z", "sha-256:item1sha"));
        RegisterCommand command4 = new RegisterCommand("assert-root-hash", Collections.singletonList("sha-256:K3rfuFF1e"));
        RegisterSerialisationFormat rsfInput = new RegisterSerialisationFormat(Iterators.forArray(command1, command2, command3, command4));

        sutService.process(rsfInput);

        verify(rsfExecutor, times(1)).execute(eq(rsfInput), any());
    }

    @Test
    public void writeTo_writesEntireRSFtoStream() {
        when(rsfCreator.create(any())).thenReturn(
                new RegisterSerialisationFormat(Iterators.forArray(
                        new RegisterCommand("assert-root-hash", Collections.singletonList("sha-256:e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855")),
                        new RegisterCommand("add-item", Collections.singletonList("{\"field-1\":\"entry1-field-1-value\",\"field-2\":\"entry1-field-2-value\"}")),
                        new RegisterCommand("add-item", Collections.singletonList("{\"field-1\":\"entry2-field-1-value\",\"field-2\":\"entry2-field-2-value\"}")),
                        new RegisterCommand("append-entry", Arrays.asList("user", "entry1-field-1-value", "2016-07-24T16:55:00Z", "sha-256:item1sha")),
                        new RegisterCommand("append-entry", Arrays.asList("user", "entry2-field-1-value", "2016-07-24T16:56:00Z", "sha-256:item2sha")),
                        new RegisterCommand("assert-root-hash", Collections.singletonList("sha-256:K3rfuFF1e")))));

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        sutService.writeTo(outputStream, rsfFormatter);

        verify(rsfCreator, times(1)).create(any());
        String expectedRSF =
                "assert-root-hash\tsha-256:e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855\n" +
                "add-item\t{\"field-1\":\"entry1-field-1-value\",\"field-2\":\"entry1-field-2-value\"}\n" +
                "add-item\t{\"field-1\":\"entry2-field-1-value\",\"field-2\":\"entry2-field-2-value\"}\n" +
                "append-entry\tuser\tentry1-field-1-value\t2016-07-24T16:55:00Z\tsha-256:item1sha\n" +
                "append-entry\tuser\tentry2-field-1-value\t2016-07-24T16:56:00Z\tsha-256:item2sha\n" +
                "assert-root-hash\tsha-256:K3rfuFF1e\n";

        String actualRSF = outputStream.toString();
        assertThat(actualRSF, Matchers.equalTo(expectedRSF));
    }

    @Test
    public void writeTo_whenCalledWithBoundary_writesPartialRSFtoStream() {
        when(rsfCreator.create(any(), eq(1), eq(2))).thenReturn(
                new RegisterSerialisationFormat(Iterators.forArray(
                        new RegisterCommand("assert-root-hash", Collections.singletonList("sha-256:K3rfuFF1e_uno")),
                        new RegisterCommand("add-item", Collections.singletonList("{\"field-1\":\"entry2-field-1-value\",\"field-2\":\"entry2-field-2-value\"}")),
                        new RegisterCommand("append-entry", Arrays.asList("user", "entry2-field-1-value", "2016-07-24T16:56:00Z", "sha-256:item2sha")),
                        new RegisterCommand("assert-root-hash", Collections.singletonList("sha-256:K3rfuFF1e_dos")))));

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        sutService.writeTo(outputStream, rsfFormatter, 1, 2);

        verify(rsfCreator, times(1)).create(any(), eq(1), eq(2));
        String expectedRSF =
                "assert-root-hash\tsha-256:K3rfuFF1e_uno\n" +
                "add-item\t{\"field-1\":\"entry2-field-1-value\",\"field-2\":\"entry2-field-2-value\"}\n" +
                "append-entry\tuser\tentry2-field-1-value\t2016-07-24T16:56:00Z\tsha-256:item2sha\n" +
                "assert-root-hash\tsha-256:K3rfuFF1e_dos\n";

        String actualRSF = outputStream.toString();
        assertThat(actualRSF, Matchers.equalTo(expectedRSF));
    }

    @Test
    public void readFrom_readsRSFFromStream() {
        String streamValue =
                "assert-root-hash\tsha-256:e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855\n" +
                "add-item\t{\"field-1\":\"entry1-field-1-value\",\"field-2\":\"entry1-field-2-value\"}\n" +
                "add-item\t{\"field-1\":\"entry2-field-1-value\",\"field-2\":\"entry2-field-2-value\"}\n" +
                "append-entry\tuser\tentry1-field-1-value\t2016-07-24T16:55:00Z\tsha-256:item1sha\n" +
                "append-entry\tuser\tentry2-field-1-value\t2016-07-24T16:56:00Z\tsha-256:item2sha\n" +
                "assert-root-hash\tsha-256:K3rfuFF1e\n";

        ByteArrayInputStream inputStream = new ByteArrayInputStream(streamValue.getBytes());

        RegisterSerialisationFormat rsfReadResult = sutService.readFrom(inputStream, rsfFormatter);

        RegisterSerialisationFormat expectedRsf = new RegisterSerialisationFormat(Iterators.forArray(
                new RegisterCommand("assert-root-hash", Collections.singletonList("sha-256:e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855")),
                new RegisterCommand("add-item", Collections.singletonList("{\"field-1\":\"entry1-field-1-value\",\"field-2\":\"entry1-field-2-value\"}")),
                new RegisterCommand("add-item", Collections.singletonList("{\"field-1\":\"entry2-field-1-value\",\"field-2\":\"entry2-field-2-value\"}")),
                new RegisterCommand("append-entry", Arrays.asList("user", "entry1-field-1-value", "2016-07-24T16:55:00Z", "sha-256:item1sha")),
                new RegisterCommand("append-entry", Arrays.asList("user", "entry2-field-1-value", "2016-07-24T16:56:00Z", "sha-256:item2sha")),
                new RegisterCommand("assert-root-hash", Collections.singletonList("sha-256:K3rfuFF1e"))));

        assertThat(Lists.newArrayList(rsfReadResult.getCommands()), equalTo(Lists.newArrayList(expectedRsf.getCommands())));
    }

    @Test
    public void readFrom_readsRSFFromStreamEscaped() throws IOException {
        try (InputStream rsfStream = Files.newInputStream(Paths.get("src/test/resources/fixtures/serialized", "valid-register-escaped.tsv"))) {
            RegisterSerialisationFormat rsfReadResult = sutService.readFrom(rsfStream, rsfFormatter);

            RegisterSerialisationFormat expectedRsf = new RegisterSerialisationFormat(Iterators.forArray(
                    new RegisterCommand("add-item", Collections.singletonList("{\"name\":\"New College\\\\New College School\",\"school\":\"402019\",\"school-authority\":\"681\",\"school-type\":\"30\"}")),
                    new RegisterCommand("append-entry", Arrays.asList("user", "402019", "2016-11-07T16:26:22Z", "sha-256:d6cca062b6a4ff7f60e401aa1ebf4bf5af51c2217916c0115d0a38a42182aec5"))));

            assertThat(Lists.newArrayList(rsfReadResult.getCommands()), equalTo(Lists.newArrayList(expectedRsf.getCommands())));
        }
    }
}
