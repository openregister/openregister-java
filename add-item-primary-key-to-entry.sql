create or replace function add_item_key_column(item_key_column text) returns void as $$
BEGIN
    IF item_key_column is NULL or item_key_column='' then
        RAISE EXCEPTION 'Item key cannot be null or empty';
    end if;

    IF not exists(select 1 from item where content ? item_key_column) then
        RAISE EXCEPTION 'Item content does not have key: %', item_key_column;
    end if;

    alter table entry add column if not exists key varchar;

    IF EXISTS(select 1 from entry where not exists(select 1 from item where item.content->>item_key_column=entry.key))
    THEN
        update entry set key = content->>item_key_column from item where entry.sha256hex=item.sha256hex;
    ELSE
        RAISE NOTICE 'Column % exist and everything is fine', item_key_column;
    END IF;

END;
$$ language plpgsql;

select add_item_key_column(:item_key);
drop function if exists add_item_key_column(item_key_column text);