group DAO;

ensureRecordTablesInPlace() ::= <<

CREATE TABLE IF NOT EXISTS  current_keys  (key VARCHAR PRIMARY KEY, entry_number INTEGER UNIQUE);

CREATE TABLE IF NOT EXISTS  total_records  (COUNT INTEGER);

--Insert query below initializes the total records for pre existing register by setting the value as no of rows in current_keys table
INSERT INTO  total_records (COUNT) SELECT (SELECT COUNT(*) FROM  current_keys) WHERE NOT EXISTS(SELECT 1 FROM  total_records );

>>
