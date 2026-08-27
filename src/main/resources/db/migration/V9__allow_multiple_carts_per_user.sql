-- Remove the one-cart-per-user uniqueness rule from existing PostgreSQL databases.
DO $$
DECLARE
    constraint_name TEXT;
BEGIN
    SELECT con.conname
    INTO constraint_name
    FROM pg_constraint con
    JOIN pg_class tbl ON tbl.oid = con.conrelid
    JOIN pg_namespace ns ON ns.oid = tbl.relnamespace
    JOIN pg_attribute attr ON attr.attrelid = tbl.oid
        AND attr.attnum = ANY (con.conkey)
    WHERE con.contype = 'u'
      AND ns.nspname = current_schema()
      AND tbl.relname = 'carts'
      AND array_length(con.conkey, 1) = 1
      AND attr.attname = 'user_id';

    IF constraint_name IS NOT NULL THEN
        EXECUTE format('ALTER TABLE carts DROP CONSTRAINT %I', constraint_name);
    END IF;
END $$;
