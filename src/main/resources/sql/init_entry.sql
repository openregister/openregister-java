group DAO;

ensureEntryTableInPlace() ::= <<

CREATE TABLE IF NOT EXISTS indexed_entry (entry_number SERIAL PRIMARY KEY, sha256hex VARCHAR, timestamp TIMESTAMP);

>>