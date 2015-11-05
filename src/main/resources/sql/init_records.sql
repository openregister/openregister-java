group DAO;

ensureRecordTablesInPlace() ::= <<
CREATE TABLE IF NOT EXISTS  current_keys  (KEY VARCHAR PRIMARY KEY, SERIAL_NUMBER INTEGER UNIQUE);

CREATE TABLE IF NOT EXISTS  total_records  (COUNT INTEGER);

--Insert query below initializes the total records for pre existing register by setting the value as no of rows in current_keys table
INSERT INTO  total_records (COUNT) SELECT (SELECT COUNT(*) FROM  current_keys) WHERE NOT EXISTS(SELECT 1 FROM  total_records );

CREATE OR REPLACE FUNCTION  total_records_fn() RETURNS TRIGGER
AS $$
BEGIN
  IF TG_OP = 'INSERT' THEN
     EXECUTE 'UPDATE total_records SET count=count + 1';
     RETURN NEW;
  END IF;
  RETURN NULL;
  END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS total_records_trigger ON  current_keys ;

CREATE TRIGGER  total_records_trigger
 AFTER INSERT ON  current_keys
 FOR EACH ROW EXECUTE PROCEDURE  total_records_fn() ;
>>
