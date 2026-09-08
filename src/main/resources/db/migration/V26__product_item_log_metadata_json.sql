ALTER TABLE product_item_logs
    ALTER COLUMN metadata TYPE JSONB USING CASE
        WHEN metadata IS NULL OR btrim(metadata) = '' THEN '{}'::jsonb
        ELSE metadata::jsonb
    END,
    ALTER COLUMN metadata SET DEFAULT '{}'::jsonb;
