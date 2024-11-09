DROP TABLE IF EXISTS account CASCADE;
CREATE TABLE IF NOT EXISTS account
(
    id    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL UNIQUE,
    name  VARCHAR(255) NOT NULL,
    password VARCHAR(255),
    roles VARCHAR(255)
);

DROP TABLE IF EXISTS resource CASCADE;
CREATE TABLE IF NOT EXISTS resource
(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    identifier VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    access BOOLEAN DEFAULT FALSE,
    category VARCHAR(10) DEFAULT 'FIGURE',
    type VARCHAR(10) DEFAULT 'VIDEO',
    account_id UUID REFERENCES account (id),
    path VARCHAR(255) NOT NULL,
    bucket VARCHAR(255) NOT NULL,
    create_time TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

DROP TABLE IF EXISTS model CASCADE;
CREATE TABLE IF NOT EXISTS model
(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    access BOOLEAN DEFAULT FALSE,
    usage VARCHAR(10) DEFAULT 'Portrait',
    status VARCHAR(10),
    detail VARCHAR(255),
    account_id UUID REFERENCES account (id),
    dataset_id UUID REFERENCES meta_human_dataset (id) ON DELETE SET NULL,
    create_time TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);