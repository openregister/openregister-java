package uk.gov.register.store.postgres;

import org.skife.jdbi.v2.sqlobject.Binder;
import org.skife.jdbi.v2.sqlobject.BinderFactory;
import org.skife.jdbi.v2.sqlobject.BindingAnnotation;
import uk.gov.register.core.Blob;

import java.lang.annotation.*;
import java.sql.SQLException;

@BindingAnnotation(BindBlob.BlobBinderFactory.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
public @interface BindBlob {
    class BlobBinderFactory implements BinderFactory {
        public Binder build(Annotation annotation) {
            return (Binder<BindBlob, Blob>) (q, bind, arg) -> {
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
