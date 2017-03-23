CREATE TABLE IF NOT EXISTS index (
  name varchar NOT NULL,
  key varchar NOT NULL,
  sha256hex VARCHAR NOT NULL,
  start_entry_number integer NOT NULL,
  end_entry_number integer,
  start_index_entry_number integer,
  end_index_entry_number integer,
  PRIMARY KEY (name, key, sha256hex, start_entry_number)
);
