group DAO;

ensureEntryTablesInPlace() ::= <<

CREATE TABLE IF NOT EXISTS   ordered_entry_index   (serial_number INTEGER PRIMARY KEY, entry JSONB);

CREATE TABLE IF NOT EXISTS   total_entries   (count INTEGER, last_updated TIMESTAMP WITHOUT TIME ZONE DEFAULT now());

--Insert query below initializes the total_entries table by 0 if it is not initialized yet
INSERT INTO   total_entries  (COUNT) SELECT 0 WHERE NOT EXISTS (SELECT 1 FROM   total_entries  );

CREATE OR REPLACE FUNCTION   total_entries_fn()   RETURNS TRIGGER
AS $$ 
BEGIN 
  IF TG_OP = 'INSERT' THEN 
     EXECUTE 'UPDATE   total_entries   SET count=count + 1, last_updated=now()';
     RETURN NEW; 
  END IF; 
  RETURN NULL; 
  END; 
$$ LANGUAGE plpgsql; 

DROP TRIGGER IF EXISTS   total_entries_trigger   ON   ordered_entry_index  ;

CREATE TRIGGER   total_entries_trigger
 AFTER INSERT ON   ordered_entry_index
 FOR EACH ROW EXECUTE PROCEDURE   total_entries_fn()  ;
>>
