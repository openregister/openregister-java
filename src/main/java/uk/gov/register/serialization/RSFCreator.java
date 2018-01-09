package uk.gov.register.serialization;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import uk.gov.register.core.Entry;
import uk.gov.register.core.HashingAlgorithm;
import uk.gov.register.core.Item;
import uk.gov.register.core.Register;
import uk.gov.register.util.HashValue;
import uk.gov.register.configuration.IndexFunctionConfiguration.IndexNames;

import java.util.*;
import java.util.stream.StreamSupport;

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
                register.getDerivationEntryIterator(IndexNames.METADATA),
                register.getEntryIterator(),
                Iterators.singletonIterator(register.getRegisterProof().getRootHash()));

        Iterator<RegisterCommand> commands = Iterators.transform(iterators, obj -> (RegisterCommand) getMapper(obj.getClass()).apply(obj));
        return new RegisterSerialisationFormat(Lists.newArrayList(commands));
    }

    public RegisterSerialisationFormat create(Register register, int totalEntries1, int totalEntries2) {
        Iterator<?> iterators;

        if (totalEntries1 == totalEntries2) {
            iterators = Iterators.singletonIterator(register.getRegisterProof(totalEntries1).getRootHash());
        } else {

            HashValue previousRootHash = totalEntries1 == 0 ? EMPTY_ROOT_HASH : register.getRegisterProof(totalEntries1).getRootHash();
            HashValue nextRootHash = register.getRegisterProof(totalEntries2).getRootHash();
            Iterator<Item> metadataItemIterator = totalEntries1 == 0 ? register.getSystemItemIterator() : Collections.emptyIterator();
            Iterator<Entry> metadataEntryIterator = totalEntries1 == 0 ? register.getDerivationEntryIterator(IndexNames.METADATA) : Collections.emptyIterator();

            iterators = Iterators.concat(
                    Iterators.singletonIterator(previousRootHash),
                    register.getItemIterator(totalEntries1, totalEntries2),
                    metadataItemIterator,
                    metadataEntryIterator,
                    register.getEntryIterator(totalEntries1, totalEntries2),
                    Iterators.singletonIterator(nextRootHash));
        }
        Iterator<RegisterCommand> commands = Iterators.transform(iterators, obj -> (RegisterCommand) getMapper(obj.getClass()).apply(obj));
        return new RegisterSerialisationFormat(Lists.newArrayList(commands));
    }

    public RegisterSerialisationFormat create(Register register, String indexName) {
        Iterator<?> iterators = Iterators.concat(
                Iterators.singletonIterator(EMPTY_ROOT_HASH),
                register.getItemIterator(),
                register.getDerivationEntryIterator(IndexNames.METADATA),
                !indexName.equals(IndexNames.METADATA) ? register.getDerivationEntryIterator(indexName) : Collections.emptyIterator());

        Iterator<RegisterCommand> commands = Iterators.transform(iterators, obj -> (RegisterCommand) getMapper(obj.getClass()).apply(obj));
        return new RegisterSerialisationFormat(Lists.newArrayList(commands));
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
        return new RegisterSerialisationFormat(Lists.newArrayList(commands));
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
