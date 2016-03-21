group DAO;

ensureItemTableInPlace() ::= <<

CREATE TABLE IF NOT EXISTS item (sha256hex VARCHAR PRIMARY KEY, content JSONB);

>>

ensureItemIndexInPlace() ::= <<

CREATE INDEX IF NOT EXISTS item_gin ON item USING GIN(content JSONB_PATH_OPS);

>>