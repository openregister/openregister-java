group DAO;

ensureItemTableInPlace() ::= <<

CREATE TABLE IF NOT EXISTS item (sha256hex VARCHAR, content BYTEA);

>>