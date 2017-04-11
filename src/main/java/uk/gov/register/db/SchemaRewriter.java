package uk.gov.register.db;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.StatementLocator;
import org.slf4j.MDC;

public class SchemaRewriter implements StatementLocator {
    @Override
    public String locate(String name, StatementContext ctx) throws Exception {
//        ctx.getConnection().setSchema("country");
        return ctx.getRawSql().replace(":schema", MDC.get("register"));
    }
}
