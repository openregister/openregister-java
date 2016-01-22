package uk.gov.register.presentation.dao;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import org.skife.jdbi.v2.tweak.ResultSetMapper;
import uk.gov.register.presentation.SignedTreeHead;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface SignedTreeHeadQueryDAO {

    @RegisterMapper(SignedTreeHeadMapper.class)
    @SqlQuery("SELECT * FROM sth")
    SignedTreeHead get();


    class SignedTreeHeadMapper implements ResultSetMapper<SignedTreeHead> {
        @Override
        public SignedTreeHead map(int index, ResultSet r, StatementContext ctx) throws SQLException {
            return new SignedTreeHead(r.getInt("tree_size"), r.getLong("timestamp"), r.getString("sha256_root_hash"), r.getString("tree_head_signature"));
        }
    }
}
