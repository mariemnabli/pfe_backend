ALTER TABLE promotion_assignments
    ADD COLUMN IF NOT EXISTS assigned_by_id BIGINT NULL,
    ADD COLUMN IF NOT EXISTS validated_by_id BIGINT NULL,
    ADD COLUMN IF NOT EXISTS validation_status VARCHAR(255) NOT NULL DEFAULT 'PENDING',
    ADD COLUMN IF NOT EXISTS validated_at DATETIME NULL;

UPDATE promotion_assignments
SET validation_status = 'VALIDATED'
WHERE validation_status IS NULL OR validation_status = '';

UPDATE promotion_assignments
SET assigned_by_id = (
    SELECT p.createur_id
    FROM promotions p
    WHERE p.id = promotion_assignments.promotion_id
)
WHERE assigned_by_id IS NULL;

UPDATE promotion_assignments
SET assigned_by_id = NULL
WHERE assigned_by_id = 0
   OR assigned_by_id NOT IN (SELECT id FROM users);

ALTER TABLE promotion_assignments
    ADD CONSTRAINT fk_promotion_assignments_assigned_by
        FOREIGN KEY (assigned_by_id) REFERENCES users(id),
    ADD CONSTRAINT fk_promotion_assignments_validated_by
        FOREIGN KEY (validated_by_id) REFERENCES users(id);
