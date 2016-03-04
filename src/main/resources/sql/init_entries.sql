group DAO;

ensureEntryTablesInPlace() ::= <<

CREATE TABLE IF NOT EXISTS   ordered_entry_index   (serial_number INTEGER PRIMARY KEY, entry JSONB);

ALTER TABLE ordered_entry_index DROP COLUMN IF EXISTS leaf_input;

CREATE TABLE IF NOT EXISTS   total_entries   (count INTEGER, last_updated TIMESTAMP WITHOUT TIME ZONE DEFAULT now());

--Insert query below initializes the total_entries table by 0 if it is not initialized yet
INSERT INTO   total_entries  (COUNT) SELECT 0 WHERE NOT EXISTS (SELECT 1 FROM   total_entries  );

DROP TRIGGER IF EXISTS total_entries_trigger ON  ordered_entry_index ;

DROP FUNCTION IF EXISTS total_entries_fn();

>>
