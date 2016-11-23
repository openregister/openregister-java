create or replace function add_item_key_to_entry(item_content_key text) returns void as $$
BEGIN
    IF item_content_key is NULL or item_content_key='' then
        RAISE EXCEPTION 'Item key parameter cannot be null or empty';
    end if;

    IF exists(select 1 from item) and not exists(select 1 from item where content ? item_content_key) then
        RAISE EXCEPTION 'Item content does not have key: %', item_content_key;
    end if;

    alter table entry add column key varchar;
    update entry set key = content->>item_content_key from item where entry.sha256hex=item.sha256hex;
END;
$$ language plpgsql;

BEGIN;
-- item content has a "primary" key which maps to the register name
select add_item_key_to_entry('${registerName}');
COMMIT;

drop function if exists add_item_key_to_entry(item_content_key text);