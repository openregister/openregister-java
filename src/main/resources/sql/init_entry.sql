group DAO;

ensureEntryTableInPlace() ::= <<

CREATE TABLE IF NOT EXISTS entry (entry_number SERIAL PRIMARY KEY, sha256hex VARCHAR, timestamp TIMESTAMP);

>>