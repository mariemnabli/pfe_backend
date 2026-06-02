ALTER TABLE clients
    ADD COLUMN IF NOT EXISTS status VARCHAR(255) NOT NULL DEFAULT 'ACTIVE',
    ADD COLUMN IF NOT EXISTS date_activation DATE NULL,
    ADD COLUMN IF NOT EXISTS date_desactivation DATE NULL;

UPDATE clients
SET status = 'ACTIVE'
WHERE status IS NULL OR TRIM(status) = '';

UPDATE clients
SET date_activation = CURRENT_DATE
WHERE status = 'ACTIVE' AND date_activation IS NULL;

UPDATE clients
SET date_desactivation = CURRENT_DATE
WHERE status = 'DESACTIVE' AND date_desactivation IS NULL;
