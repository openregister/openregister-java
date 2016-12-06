package uk.gov.register.store.postgres;

import org.skife.jdbi.v2.sqlobject.Binder;
import org.skife.jdbi.v2.sqlobject.BinderFactory;
import org.skife.jdbi.v2.sqlobject.BindingAnnotation;
import uk.gov.register.core.Item;

import java.lang.annotation.*;
import java.sql.SQLException;

@BindingAnnotation(BindItem.ItemBinderFactory.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
public @interface BindItem {
    class ItemBinderFactory implements BinderFactory {
        public Binder build(Annotation annotation) {
            return (Binder<BindItem, Item>) (q, bind, arg) -> {
                q.bind("sha256hex", arg.getSha256hex().getValue());
                try {
                    q.bind("content", arg.getContentAsJsonb());
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            };
        }
    }
}
