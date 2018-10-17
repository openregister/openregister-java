package uk.gov.register.store.postgres;

import org.skife.jdbi.v2.sqlobject.Binder;
import org.skife.jdbi.v2.sqlobject.BinderFactory;
import org.skife.jdbi.v2.sqlobject.BindingAnnotation;
import uk.gov.register.core.Entry;

import java.lang.annotation.*;

@BindingAnnotation(BindEntry.EntryBinderFactory.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
public @interface BindEntry {
    class EntryBinderFactory implements BinderFactory {
        public Binder build(Annotation annotation) {
            return (Binder<BindEntry, Entry>) (q, bind, arg) -> {
                q.bind("entry_number", arg.getEntryNumber());
                q.bind("timestampAsLong", arg.getTimestampAsLong());
                q.bind("key", arg.getKey());
                q.bind("entryType", arg.getEntryType());
            };
        }
    }
}
