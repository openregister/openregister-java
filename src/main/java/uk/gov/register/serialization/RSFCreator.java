package uk.gov.register.serialization;

import uk.gov.register.core.HashingAlgorithm;
import uk.gov.register.core.Register;
import uk.gov.register.util.HashValue;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.google.common.collect.Iterators;

public class RSFCreator {

    private final HashValue EMPTY_ROOT_HASH = new HashValue(HashingAlgorithm.SHA256, "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855");

    private Map<Class, RegisterCommandMapper> registeredMappers;

    public RSFCreator() {
        this.registeredMappers = new HashMap<>();
    }

    public RegisterSerialisationFormat create(Register register) {
        Iterator<?> iterators = Iterators.concat(
                Iterators.singletonIterator(EMPTY_ROOT_HASH),
                register.getItemIterator(),
                register.getEntryIterator(),
                Iterators.singletonIterator(register.getRegisterProof().getRootHash()));

        Iterator<RegisterCommand> commands = Iterators.transform(iterators, obj -> (RegisterCommand) getMapper(obj.getClass()).apply(obj));
        return new RegisterSerialisationFormat(commands);
    }

    public RegisterSerialisationFormat create(Register register, int totalEntries1, int totalEntries2) {
        Iterator<?> iterators;

        if (totalEntries1 == totalEntries2) {
            iterators = Iterators.singletonIterator(register.getRegisterProof(totalEntries1));
        } else {

            HashValue previousRootHash = totalEntries1 == 0 ? EMPTY_ROOT_HASH : register.getRegisterProof(totalEntries1).getRootHash();
            HashValue nextRootHash = register.getRegisterProof(totalEntries2).getRootHash();

            iterators = Iterators.concat(
                    Iterators.singletonIterator(previousRootHash),
                    register.getItemIterator(totalEntries1, totalEntries2),
                    register.getEntryIterator(totalEntries1, totalEntries2),
                    Iterators.singletonIterator(nextRootHash));
        }
        Iterator<RegisterCommand> commands = Iterators.transform(iterators, obj -> (RegisterCommand) getMapper(obj.getClass()).apply(obj));
        return new RegisterSerialisationFormat(commands);
    }

    public RegisterSerialisationFormat create(Register register, String indexName) {
        Iterator<?> iterators = Iterators.concat(
                register.getItemIterator(),
                register.getDerivationEntryIterator(indexName));

        Iterator<RegisterCommand> commands = Iterators.transform(iterators, obj -> (RegisterCommand) getMapper(obj.getClass()).apply(obj));
        return new RegisterSerialisationFormat(commands);
    }


    public RegisterSerialisationFormat create(Register register, String indexName, int totalEntries1, int totalEntries2) {
        Iterator<?> iterators;

        if (totalEntries1 == totalEntries2) {
            iterators = Collections.emptyIterator();
        } else {
            iterators = Iterators.concat(
                    register.getItemIterator(totalEntries1, totalEntries2),
                    register.getDerivationEntryIterator(indexName, totalEntries1, totalEntries2));
        }
        Iterator<RegisterCommand> commands = Iterators.transform(iterators, obj -> (RegisterCommand) getMapper(obj.getClass()).apply(obj));
        return new RegisterSerialisationFormat(commands);
    }

    public void register(RegisterCommandMapper commandMapper) {
        registeredMappers.put(commandMapper.getMapClass(), commandMapper);
    }

    private RegisterCommandMapper getMapper(Class objClass) {
        if (registeredMappers.containsKey(objClass)) {
            return registeredMappers.get(objClass);
        } else {
            throw new RuntimeException("Mapper not registered for class: " + objClass.getName());
        }
    }
}
