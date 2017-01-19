package uk.gov.register.serialization;

import java.lang.reflect.ParameterizedType;
import java.util.function.Function;

public abstract class RegisterCommandMapper<A, B extends RegisterCommand> implements Function<A, B> {

    public Class<A> getMapClass() {
        try {
            String className = ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0].getTypeName();
            Class<?> clazz = Class.forName(className);
            return (Class<A>) clazz;
        } catch (Exception e) {
            throw new IllegalStateException("Class is not parametrized with generic type!!! Please use extends <> ");
        }
    }
}

