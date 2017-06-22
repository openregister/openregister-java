package uk.gov.register.db;

import org.skife.jdbi.v2.Binding;
import org.skife.jdbi.v2.ColonPrefixNamedParamStatementRewriter;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.RewrittenStatement;
import org.skife.jdbi.v2.tweak.StatementRewriter;

public class SubstituteSchemaRewriter implements StatementRewriter {
    private ColonPrefixNamedParamStatementRewriter colonRewriter;

    public SubstituteSchemaRewriter() {
        colonRewriter = new ColonPrefixNamedParamStatementRewriter();
    }

    @Override
    public RewrittenStatement rewrite(String sql, Binding params, StatementContext ctx) {
        String sqlWithSchema = sql.replaceAll(":schema", params.forName("schema").toString().replace("'","\""));
        return colonRewriter.rewrite(sqlWithSchema, params, ctx);
    }
}
