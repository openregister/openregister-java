package uk.gov.register.serialization;

import com.google.common.collect.Iterators;
import uk.gov.register.core.HashingAlgorithm;
import uk.gov.register.core.Register;
import uk.gov.register.util.HashValue;
import uk.gov.register.views.RegisterProof;

import javax.inject.Provider;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class RSFCreator {

    private final String EMPTY_ROOT_HASH = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";
    private final RegisterProof emptyRegisterProof;

    private Map<Class, RegisterCommandMapper> registeredMappers;

    public RSFCreator() {
        this.registeredMappers = new HashMap<>();
        this.emptyRegisterProof = new RegisterProof(new HashValue(HashingAlgorithm.SHA256, EMPTY_ROOT_HASH));
    }

    public RegisterSerialisationFormat create(Register register) {
//        Stream<RegisterCommand> registerCommandStream = Stream.of(
//                (Provider<Iterator<?>>) () -> Iterators.singletonIterator(emptyRegisterProof),
//                (Provider<Iterator<?>>) () -> register.getItemIterator(),
//                (Provider<Iterator<?>>) () -> register.getEntryIterator(),
//                (Provider<Iterator<?>>) () -> {
//                    try {
//                        return Iterators.singletonIterator(register.getRegisterProof());
//                    } catch (NoSuchAlgorithmException e) {
//                        throw new RuntimeException(e);
//                    }
//                }).parallel()
//                .flatMap((iteratorProvider) -> iteratorToStream(iteratorProvider.get())
//                        .map(obj -> (RegisterCommand) registeredMappers.get(obj.getClass()).apply(obj)));

        Stream<RegisterCommand> registerCommandStream = Stream.of(
                (Provider<Iterator<?>>) () -> Iterators.singletonIterator(emptyRegisterProof),
                (Provider<Iterator<?>>) register::getItemIterator,
                (Provider<Iterator<?>>) register::getEntryIterator,
                (Provider<Iterator<?>>) () -> {
                    try {
//                        return Iterators.singletonIterator(register.getRegisterProof());
                        return Iterators.singletonIterator(emptyRegisterProof);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }).parallel()
                .map(Provider::get)
                .collect(Collectors.toList())
                .stream().flatMap(iterator -> iteratorToStream(iterator)
                        .map(obj -> (RegisterCommand) registeredMappers.get(obj.getClass()).apply(obj)));


//                .flatMap((iteratorProvider) -> iteratorToStream(iteratorProvider.get())
//                        .map(obj -> (RegisterCommand) registeredMappers.get(obj.getClass()).apply(obj)));


        ;
//    }).parallel().flatMap(p -> Iterators.transform(p.get(), obj -> (RegisterCommand) registeredMappers.get(obj.getClass()).apply(obj))).;
//                .map(obj -> obj.
//                        (RegisterCommand) registeredMappers.get(obj.getClass()).apply(obj));


//            Iterator<?> iterators = Iterators.concat(
//                    Iterators.singletonIterator(emptyRegisterProof),
//                    register.getItemIterator(),
//                    register.getEntryIterator(),
//                    Iterators.singletonIterator(register.getRegisterProof()));
//
//            Iterator<RegisterCommand> commands = Iterators.transform(iteratorOfIterators, obj -> (RegisterCommand) registeredMappers.get(obj.getClass()).apply(obj));

        return new RegisterSerialisationFormat(registerCommandStream.iterator());
    }

    private <T> Stream<T> iteratorToStream(Iterator<T> iterator) {
        Iterable<T> iterable = () -> iterator;
        return StreamSupport.stream(iterable.spliterator(), false);
    }

    public RegisterSerialisationFormat create(Register register, int totalEntries1, int totalEntries2) {
        Iterator<?> iterators;

        if (totalEntries1 == totalEntries2) {
            iterators = Iterators.singletonIterator(register.getRegisterProof(totalEntries1));
        } else {

            RegisterProof previousRegisterProof = totalEntries1 == 0 ? emptyRegisterProof : register.getRegisterProof(totalEntries1);
            RegisterProof nextRegisterProof = register.getRegisterProof(totalEntries2);

            iterators = Iterators.concat(
                    Iterators.singletonIterator(previousRegisterProof),
                    register.getItemIterator(totalEntries1, totalEntries2),
                    register.getEntryIterator(totalEntries1, totalEntries2),
                    Iterators.singletonIterator(nextRegisterProof));
        }
        Iterator<RegisterCommand> commands = Iterators.transform(iterators, obj -> (RegisterCommand) registeredMappers.get(obj.getClass()).apply(obj));
        return new RegisterSerialisationFormat(commands);
    }

    public void register(RegisterCommandMapper commandMapper) {
        registeredMappers.put(commandMapper.getMapClass(), commandMapper);
    }
}
