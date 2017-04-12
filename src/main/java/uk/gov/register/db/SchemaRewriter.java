package uk.gov.register.db;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.StatementLocator;

public class SchemaRewriter implements StatementLocator {
    public static ThreadLocal<String> schema = new ThreadLocal<>();

    @Override
    public String locate(String name, StatementContext ctx) throws Exception {
        return ctx.getRawSql().replace(":schema", schema.get());
    }
}