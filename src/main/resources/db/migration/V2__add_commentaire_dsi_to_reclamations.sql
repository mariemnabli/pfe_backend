ALTER TABLE reclamations
    ADD COLUMN IF NOT EXISTS commentaire_dsi VARCHAR(1000) NULL;
