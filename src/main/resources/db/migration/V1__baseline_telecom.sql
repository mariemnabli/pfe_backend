CREATE TABLE IF NOT EXISTS users (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255),
    email VARCHAR(255),
    password VARCHAR(255),
    role VARCHAR(255),
    enabled BIT(1),
    premiere_connexion BIT(1),
    first_time_connexion DATETIME NULL,
    reset_token VARCHAR(255),
    reset_token_expiry DATETIME NULL,
    refresh_token VARCHAR(255),
    refresh_token_expiry TIMESTAMP NULL,
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT uk_users_refresh_token UNIQUE (refresh_token)
);

CREATE TABLE IF NOT EXISTS customer_groups (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    group_code VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    group_type VARCHAR(255) NOT NULL,
    status VARCHAR(255) NOT NULL,
    CONSTRAINT uk_customer_groups_group_code UNIQUE (group_code)
);

CREATE TABLE IF NOT EXISTS clients (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    customer_id VARCHAR(255),
    nom VARCHAR(255),
    prenom VARCHAR(255),
    telephone VARCHAR(255),
    email VARCHAR(255),
    adresse VARCHAR(255),
    ville VARCHAR(255),
    document_type INT NOT NULL,
    cin_number VARCHAR(255),
    cin_image_path VARCHAR(255),
    passport_number VARCHAR(255),
    passport_image_path VARCHAR(255),
    CONSTRAINT uk_clients_customer_id UNIQUE (customer_id),
    CONSTRAINT uk_clients_cin_number UNIQUE (cin_number),
    CONSTRAINT uk_clients_passport_number UNIQUE (passport_number)
);

CREATE TABLE IF NOT EXISTS plans_tarifaires (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(255),
    prix_mensuel DOUBLE,
    description VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS services (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    nom_service VARCHAR(255),
    description VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS offres (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    nom_offre VARCHAR(255),
    type_offre VARCHAR(255),
    plan_tarifaire_id BIGINT,
    CONSTRAINT fk_offres_plan_tarifaire
        FOREIGN KEY (plan_tarifaire_id) REFERENCES plans_tarifaires(id)
);

CREATE TABLE IF NOT EXISTS offre_services (
    offre_id BIGINT NOT NULL,
    service_id BIGINT NOT NULL,
    PRIMARY KEY (offre_id, service_id),
    CONSTRAINT fk_offre_services_offre
        FOREIGN KEY (offre_id) REFERENCES offres(id),
    CONSTRAINT fk_offre_services_service
        FOREIGN KEY (service_id) REFERENCES services(id)
);

CREATE TABLE IF NOT EXISTS contrats (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    contract_id VARCHAR(255),
    contract_type VARCHAR(255) NOT NULL DEFAULT 'INDIVIDUAL',
    holder_type VARCHAR(255) NOT NULL DEFAULT 'CUSTOMER',
    date_debut DATE NULL,
    date_fin DATE NULL,
    statut VARCHAR(255),
    directory_number BIGINT NULL,
    client_id BIGINT NULL,
    customer_group_id BIGINT NULL,
    offre_id BIGINT NULL,
    CONSTRAINT uk_contrats_contract_id UNIQUE (contract_id),
    CONSTRAINT fk_contrats_client
        FOREIGN KEY (client_id) REFERENCES clients(id),
    CONSTRAINT fk_contrats_group
        FOREIGN KEY (customer_group_id) REFERENCES customer_groups(id),
    CONSTRAINT fk_contrats_offre
        FOREIGN KEY (offre_id) REFERENCES offres(id)
);

CREATE TABLE IF NOT EXISTS promotions (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    nom_promotion VARCHAR(255),
    type_reduction VARCHAR(255),
    valeur_reduction DOUBLE,
    date_debut DATE NULL,
    date_fin DATE NULL,
    statut VARCHAR(255),
    regle_eligibilite VARCHAR(255),
    anciennete_minimale INT NULL,
    createur_id BIGINT NULL,
    validateur_id BIGINT NULL,
    CONSTRAINT fk_promotions_createur
        FOREIGN KEY (createur_id) REFERENCES users(id),
    CONSTRAINT fk_promotions_validateur
        FOREIGN KEY (validateur_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS promotion_assignments (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    promotion_id BIGINT NOT NULL,
    target_type VARCHAR(255) NOT NULL,
    target_customer_id BIGINT NULL,
    target_group_id BIGINT NULL,
    target_contract_id BIGINT NULL,
    status VARCHAR(255) NOT NULL,
    assignment_mode VARCHAR(255) NOT NULL,
    effective_start_date DATE NOT NULL,
    effective_end_date DATE NULL,
    inherited_to_members BIT(1) NOT NULL,
    assigned_at DATETIME NOT NULL,
    CONSTRAINT fk_promotion_assignments_promotion
        FOREIGN KEY (promotion_id) REFERENCES promotions(id),
    CONSTRAINT fk_promotion_assignments_customer
        FOREIGN KEY (target_customer_id) REFERENCES clients(id),
    CONSTRAINT fk_promotion_assignments_group
        FOREIGN KEY (target_group_id) REFERENCES customer_groups(id),
    CONSTRAINT fk_promotion_assignments_contract
        FOREIGN KEY (target_contract_id) REFERENCES contrats(id)
);

CREATE TABLE IF NOT EXISTS customer_group_members (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    customer_group_id BIGINT NOT NULL,
    customer_id BIGINT NOT NULL,
    member_role VARCHAR(255) NOT NULL,
    joined_at DATE NOT NULL,
    left_at DATE NULL,
    primary_member BIT(1) NOT NULL,
    status VARCHAR(255) NOT NULL,
    CONSTRAINT fk_customer_group_members_group
        FOREIGN KEY (customer_group_id) REFERENCES customer_groups(id),
    CONSTRAINT fk_customer_group_members_customer
        FOREIGN KEY (customer_id) REFERENCES clients(id)
);

CREATE TABLE IF NOT EXISTS reclamations (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    description VARCHAR(1000) NOT NULL,
    statut VARCHAR(255),
    date_creation DATETIME NULL,
    date_mise_ajour DATETIME NULL,
    commentaire_vendeur VARCHAR(255),
    client_id BIGINT NULL,
    CONSTRAINT fk_reclamations_client
        FOREIGN KEY (client_id) REFERENCES clients(id)
);

CREATE TABLE IF NOT EXISTS souscriptions_promotions (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    date_souscription DATE NULL,
    statut VARCHAR(255),
    contrat_id BIGINT NULL,
    promotion_id BIGINT NULL,
    CONSTRAINT fk_souscriptions_contrat
        FOREIGN KEY (contrat_id) REFERENCES contrats(id),
    CONSTRAINT fk_souscriptions_promotion
        FOREIGN KEY (promotion_id) REFERENCES promotions(id)
);

ALTER TABLE contrats
    ADD COLUMN IF NOT EXISTS contract_type VARCHAR(255) NOT NULL DEFAULT 'INDIVIDUAL',
    ADD COLUMN IF NOT EXISTS holder_type VARCHAR(255) NOT NULL DEFAULT 'CUSTOMER',
    ADD COLUMN IF NOT EXISTS customer_group_id BIGINT NULL;

ALTER TABLE clients
    ADD COLUMN IF NOT EXISTS customer_id VARCHAR(255) NULL;

ALTER TABLE users
    ADD COLUMN IF NOT EXISTS refresh_token VARCHAR(255) NULL,
    ADD COLUMN IF NOT EXISTS refresh_token_expiry TIMESTAMP NULL;
