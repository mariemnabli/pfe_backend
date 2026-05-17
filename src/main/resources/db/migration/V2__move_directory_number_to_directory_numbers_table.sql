CREATE TABLE IF NOT EXISTS directory_numbers (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    numero BIGINT NOT NULL,
    status VARCHAR(255) NOT NULL,
    date_activation DATE NULL,
    date_desactivation DATE NULL,
    contrat_id BIGINT NULL,
    CONSTRAINT uk_directory_numbers_numero UNIQUE (numero),
    CONSTRAINT fk_directory_numbers_contrat
        FOREIGN KEY (contrat_id) REFERENCES contrats(id)
);

INSERT INTO directory_numbers (numero, status, date_activation, date_desactivation, contrat_id)
SELECT c.directory_number,
       CASE
           WHEN SUM(CASE WHEN c.statut = 'ACTIF' THEN 1 ELSE 0 END) > 0 THEN 'ACTIF'
           ELSE 'DESACTIVE'
       END,
       MIN(c.date_debut),
       MAX(CASE WHEN c.statut IN ('RESILIE', 'SUSPENDU') THEN c.date_fin ELSE NULL END),
       MIN(c.id)
FROM contrats c
WHERE c.directory_number IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM directory_numbers dn
      WHERE dn.numero = c.directory_number
  )
GROUP BY c.directory_number;

ALTER TABLE contrats
    DROP COLUMN directory_number;
