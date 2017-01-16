package uk.gov.register.service;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import uk.gov.register.core.Register;
import uk.gov.register.core.RegisterContext;
import uk.gov.register.serialization.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collections;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Matchers.any;
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
        when(registerContext.buildOnDemandRegister()).thenReturn(register);

        doAnswer(new Answer<Void>() {
            public Void answer(InvocationOnMock invocation) {
                Consumer<Register> callback = (Consumer<Register>) invocation.getArguments()[0];
                callback.accept(register);
                return null;
            }
        }).when(registerContext).transactionalRegisterOperation(any(Consumer.class));

        doAnswer(new Answer<Void>() {
            public Void answer(InvocationOnMock invocation) {
                Function<Register, Boolean> callback = (Function<Register, Boolean>) invocation.getArguments()[0];
                callback.apply(register);
                return null;
            }
        }).when(registerContext).transactionalRegisterOperation(any(Function.class));

        sutService = new RegisterSerialisationFormatService(registerContext, rsfExecutor, rsfCreator);
        rsfFormatter = new RSFFormatter();
    }

    @Test
    public void processRegisterComponents() {
        RegisterCommand command1 = new RegisterCommand("assert-root-hash", Collections.singletonList("sha-256:e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"));
        RegisterCommand command2 = new RegisterCommand("add-item", Collections.singletonList("{\"field-1\":\"entry1-field-1-value\",\"field-2\":\"entry1-field-2-value\"}"));
        RegisterCommand command3 = new RegisterCommand("append-entry", Arrays.asList("2016-07-24T16:55:00Z", "sha-256:item1sha", "entry1-field-1-value"));
        RegisterCommand command4 = new RegisterCommand("assert-root-hash", Collections.singletonList("sha-256:K3rfuFF1e"));
        RegisterSerialisationFormat rsfInput = new RegisterSerialisationFormat(Iterators.forArray(command1, command2, command3, command4));

        when(rsfExecutor.execute(eq(rsfInput), any())).thenReturn(RSFResult.createSuccessResult());

        RSFResult rsfResult = sutService.processRegisterSerialisationFormat(rsfInput);

        verify(rsfExecutor, times(1)).execute(eq(rsfInput), any());
        assertThat(rsfResult, equalTo(RSFResult.createSuccessResult()));
    }

    @Test
    public void writeTo_writesEntireRSFtoStream() throws NoSuchAlgorithmException {
        when(rsfCreator.create(any())).thenReturn(
                new RegisterSerialisationFormat(Iterators.forArray(
                        new RegisterCommand("assert-root-hash", Collections.singletonList("sha-256:e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855")),
                        new RegisterCommand("add-item", Collections.singletonList("{\"field-1\":\"entry1-field-1-value\",\"field-2\":\"entry1-field-2-value\"}")),
                        new RegisterCommand("add-item", Collections.singletonList("{\"field-1\":\"entry2-field-1-value\",\"field-2\":\"entry2-field-2-value\"}")),
                        new RegisterCommand("append-entry", Arrays.asList("2016-07-24T16:55:00Z", "sha-256:item1sha", "entry1-field-1-value")),
                        new RegisterCommand("append-entry", Arrays.asList("2016-07-24T16:56:00Z", "sha-256:item2sha", "entry2-field-1-value")),
                        new RegisterCommand("assert-root-hash", Collections.singletonList("sha-256:K3rfuFF1e")))));

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        sutService.writeTo(outputStream, rsfFormatter);

        verify(rsfCreator, times(1)).create(any());
        String expectedRSF =
                "assert-root-hash\tsha-256:e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855\n" +
                "add-item\t{\"field-1\":\"entry1-field-1-value\",\"field-2\":\"entry1-field-2-value\"}\n" +
                "add-item\t{\"field-1\":\"entry2-field-1-value\",\"field-2\":\"entry2-field-2-value\"}\n" +
                "append-entry\t2016-07-24T16:55:00Z\tsha-256:item1sha\tentry1-field-1-value\n" +
                "append-entry\t2016-07-24T16:56:00Z\tsha-256:item2sha\tentry2-field-1-value\n" +
                "assert-root-hash\tsha-256:K3rfuFF1e\n";

        String actualRSF = outputStream.toString();
        assertThat(actualRSF, Matchers.equalTo(expectedRSF));
    }

    @Test
    public void writeTo_whenCalledWithBoundary_writesPartialRSFtoStream() throws NoSuchAlgorithmException {
        when(rsfCreator.create(any(), eq(1), eq(2))).thenReturn(
                new RegisterSerialisationFormat(Iterators.forArray(
                        new RegisterCommand("assert-root-hash", Collections.singletonList("sha-256:K3rfuFF1e_uno")),
                        new RegisterCommand("add-item", Collections.singletonList("{\"field-1\":\"entry2-field-1-value\",\"field-2\":\"entry2-field-2-value\"}")),
                        new RegisterCommand("append-entry", Arrays.asList("2016-07-24T16:56:00Z", "sha-256:item2sha", "entry2-field-1-value")),
                        new RegisterCommand("assert-root-hash", Collections.singletonList("sha-256:K3rfuFF1e_dos")))));

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        sutService.writeTo(outputStream, rsfFormatter, 1, 2);

        verify(rsfCreator, times(1)).create(any(), eq(1), eq(2));
        String expectedRSF =
                "assert-root-hash\tsha-256:K3rfuFF1e_uno\n" +
                "add-item\t{\"field-1\":\"entry2-field-1-value\",\"field-2\":\"entry2-field-2-value\"}\n" +
                "append-entry\t2016-07-24T16:56:00Z\tsha-256:item2sha\tentry2-field-1-value\n" +
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
                        "append-entry\t2016-07-24T16:55:00Z\tsha-256:item1sha\tentry1-field-1-value\n" +
                        "append-entry\t2016-07-24T16:56:00Z\tsha-256:item2sha\tentry2-field-1-value\n" +
                        "assert-root-hash\tsha-256:K3rfuFF1e\n";

        ByteArrayInputStream inputStream = new ByteArrayInputStream(streamValue.getBytes());

        RegisterSerialisationFormat rsfReadResult = sutService.readFrom(inputStream, rsfFormatter);

        RegisterSerialisationFormat expectedRsf = new RegisterSerialisationFormat(Iterators.forArray(
                new RegisterCommand("assert-root-hash", Collections.singletonList("sha-256:e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855")),
                new RegisterCommand("add-item", Collections.singletonList("{\"field-1\":\"entry1-field-1-value\",\"field-2\":\"entry1-field-2-value\"}")),
                new RegisterCommand("add-item", Collections.singletonList("{\"field-1\":\"entry2-field-1-value\",\"field-2\":\"entry2-field-2-value\"}")),
                new RegisterCommand("append-entry", Arrays.asList("2016-07-24T16:55:00Z", "sha-256:item1sha", "entry1-field-1-value")),
                new RegisterCommand("append-entry", Arrays.asList("2016-07-24T16:56:00Z", "sha-256:item2sha", "entry2-field-1-value")),
                new RegisterCommand("assert-root-hash", Collections.singletonList("sha-256:K3rfuFF1e"))));

        assertThat(Lists.newArrayList(rsfReadResult.getCommands()), equalTo(Lists.newArrayList(expectedRsf.getCommands())));
    }
}