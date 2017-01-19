package uk.gov.register.serialization;

import com.google.common.collect.Iterators;
import uk.gov.register.core.HashingAlgorithm;
import uk.gov.register.core.Register;
import uk.gov.register.util.HashValue;
import uk.gov.register.views.RegisterProof;

import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class RSFCreator {

    private final String EMPTY_ROOT_HASH = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";
    private final RegisterProof emptyRegisterProof;

    private Map<Class, RegisterCommandMapper> registeredMappers;

    public RSFCreator() {
        this.registeredMappers = new HashMap<>();
        this.emptyRegisterProof = new RegisterProof(new HashValue(HashingAlgorithm.SHA256, EMPTY_ROOT_HASH));
    }

    public RegisterSerialisationFormat create(Register register) {
        try {
            Iterator<?> iterators = Iterators.concat(
                    Iterators.singletonIterator(emptyRegisterProof),
                    register.getItemIterator(),
                    register.getEntryIterator(),
                    Iterators.singletonIterator(register.getRegisterProof()));

            Iterator<RegisterCommand> commands = Iterators.transform(iterators, obj -> (RegisterCommand) getMapper(obj.getClass()).apply(obj));
            return new RegisterSerialisationFormat(commands);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
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
